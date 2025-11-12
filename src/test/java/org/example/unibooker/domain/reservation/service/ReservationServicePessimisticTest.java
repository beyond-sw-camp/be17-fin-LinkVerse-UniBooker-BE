package org.example.unibooker.domain.reservation.service;

import org.example.unibooker.domain.reservation.model.dto.ReservationDto;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ReservationServicePessimisticTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ResourceRepository resourceRepository;

    private Long testResourceId;
    private Long baseUserId;

    @BeforeEach
    void setUp() {
        // 실제 DB의 리소스 ID와 사용자 ID를 테스트용으로 지정
        testResourceId = 26000L;
        baseUserId = 7030L;

        assertThat(resourceRepository.findById(testResourceId)).isPresent();
    }


    @Test
    @DisplayName("여러 스레드에서 동시 예약해도 비관적 락을 사용하여 동시성 이슈가 발생하지 않음")
    void 서로_다른_사용자_100명_동시_예약_요청_테스트() throws InterruptedException {

        int threadCount = 100; // 스레드 개수
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount); // 100개의 스레드를 생성해 동시에 예약 요청 실행
        CountDownLatch latch = new CountDownLatch(threadCount); // 모든 스레드가 끝날 때까지 메인 스레드 대기
        List<Boolean> results = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            long userId = baseUserId + i; // 테스트용 사용자 ID

            executorService.submit(() -> {
                try {
                    ReservationDto.Request dto = ReservationDto.Request.builder()
                            .date(LocalDate.of(2025, 11, 11))
                            .time(LocalTime.of(11, 0))
                            .headCount(1)
                            .customFieldValues(Collections.emptyList())
                            .build();
                    reservationService.reserve(dto, testResourceId, userId);
                    results.add(true);

                } catch (Exception e) {
                    e.printStackTrace();
                    results.add(false);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 종료 대기
        executorService.shutdown();


        /** =========================== 결과 검증 =========================== */
        long successCount = results.stream().filter(r -> r).count();
        long failCount = results.stream().filter(r -> !r).count();

        System.out.println("✅ 성공한 요청 수: " + successCount);
        System.out.println("❌ 실패한 요청 수: " + failCount);

        assertThat(successCount).isEqualTo(1);
        assertThat(failCount).isEqualTo(99);
    }
}
