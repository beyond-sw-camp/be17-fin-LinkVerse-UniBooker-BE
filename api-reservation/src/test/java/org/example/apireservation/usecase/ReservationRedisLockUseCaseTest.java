package org.example.apireservation.usecase;

import org.example.apireservation.domain.model.Resource;
import org.example.apireservation.domain.model.ServiceCategory;
import org.example.apireservation.domain.model.entity.Reservations;
import org.example.apireservation.infrastructure.ResourceFeignAdapter;
import org.example.apireservation.usecase.impl.ReservationUseCase;
import org.example.apireservation.usecase.port.in.ReservationCommand;
import org.example.apireservation.usecase.port.out.ReservationPersistencePort;
import org.example.common.base.BaseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
public class ReservationRedisLockUseCaseTest {

    @MockBean
    private ResourceFeignAdapter resourceFeignClient;

    @Autowired
    private ReservationUseCase reservationUseCase;

    @Autowired
    private RedissonClient redissonClient;

    private Long testResourceId;
    private Long baseUserId;

    @BeforeEach
    void setUp() {
        testResourceId = 1L; // 테스트 리소스 ID
        baseUserId = 4L;      // 테스트 시작 사용자 ID

        Resource fakeResource = Resource.builder()
                .id(testResourceId)
                .name("테스트 회의실 A")
                .category(ServiceCategory.SEAT)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .row(2)
                .col(5)
                .timeInterval(30)
                .capacity(10)
                .status("ACTIVE")
                .resourceGroupId(4L)
                .resourceGroupName("테스트 리소스 그룹")
                .build();


        Mockito.when(resourceFeignClient.findResourceByIdForUpdate(testResourceId))
                .thenReturn(BaseResponse.success(fakeResource));

        assertThat(redissonClient).isNotNull();
    }


    @Test
    @DisplayName("Redis 분산락 100명 동시 예약 요청 테스트 (1명만 성공)")
    void redisLock_서로_다른_사용자_동시_예약_요청_테스트() throws InterruptedException {

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Boolean> results = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            long userId = baseUserId + i;

            executorService.submit(() -> {
                try {
                    ReservationCommand dto = ReservationCommand.builder()
                            .date(LocalDate.of(2025, 11, 20))
                            .time(LocalTime.of(11, 0))
                            .headCount(1)
                            .row(2)
                            .col(5)
                            .customFieldValues(Collections.emptyList())
                            .build();

                    reservationUseCase.reserve(dto, testResourceId, userId, 2L);
                    results.add(true);

                } catch (Exception e) {
                    e.printStackTrace();
                    results.add(false);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();


        /** =========================== 결과 검증 =========================== */
        long successCount = results.stream().filter(r -> r).count();
        long failCount = results.stream().filter(r -> !r).count();

        System.out.println("✅ 성공: " + successCount);
        System.out.println("❌ 실패: " + failCount);

        // Redis 분산락이 정확히 동작하면 성공은 1명
        assertThat(successCount).isEqualTo(1);
        assertThat(failCount).isEqualTo(99);
    }
}
