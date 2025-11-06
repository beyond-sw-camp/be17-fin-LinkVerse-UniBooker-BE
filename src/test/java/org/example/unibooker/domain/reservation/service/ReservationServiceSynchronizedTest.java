package org.example.unibooker.domain.reservation.service;

import org.example.unibooker.domain.reservation.model.dto.ReservationDto;
import org.example.unibooker.domain.reservation.model.entity.Reservations;
import org.example.unibooker.domain.reservation.repository.ReservationRepository;
import org.example.unibooker.domain.resource.repository.ResourceRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReservationServiceSynchronizedTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    private Long testResourceId;
    private Long baseUserId;

    /**
     * @BeforeEach : 각 테스트 실행 전마다 실행
     * @BeforeAll : 클래스 내 테스트 전 전체 1회 실행, static 필요
     */
    @BeforeEach
    void setUp() {

        // 실제 DB의 리소스 ID와 사용자 ID를 테스트용으로 지정
        testResourceId = 26000L;
        baseUserId = 7030L;

        assertThat(resourceRepository.findById(testResourceId)).isPresent();
//        reservationRepository.deleteAll(); // 기존 예약 데이터 초기화
    }


    @Test
    @DisplayName("여러 스레드에서 synchronized 메소드에 접근해 동시 예약해도 동시성 이슈가 발생하지 않음")
    void 서로_다른_사용자_100명_동시_예약_요청_테스트() throws InterruptedException {

        int threadCount = 100; // 스레드 개수
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount); // 100개의 스레드를 생성해 동시에 예약 요청 실행
        CountDownLatch latch = new CountDownLatch(threadCount); // 모든 스레드가 끝날 때까지 메인 스레드 대기

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            long userId = baseUserId + i; // 테스트용 사용자 ID

            futures.add(executorService.submit(() -> {
                try {
                    ReservationDto.Request dto = ReservationDto.Request.builder()
                            .date(LocalDate.of(2025, 11, 11))
                            .time(LocalTime.of(11, 0))
                            .headCount(1)
                            .customFieldValues(Collections.emptyList())
                            .build();
                    synchronized (this) {
                        reservationService.reserve(dto, testResourceId, userId);
                    }
                } catch (Exception e) {
                    System.out.println("Thread " + userId + " 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await(); // 모든 스레드 종료 대기
        executorService.shutdown();


        /** =========================== 결과 검증 =========================== */
        List<Reservations> allReservations = reservationRepository.findAll();
        System.out.println("총 예약 수: " + allReservations.size());

        // 동시성 제어가 제대로 되면 1건만 예약됨
        assertThat(allReservations.size()).isEqualTo(1); // synchronized 덕분에 하나만 성공해야 함
    }
}