package org.example.apireservation.usecase.port.out;

public interface ReservationKafkaPort {

    // 예약 완료 시 api-app 서버로 예약 완료 이벤트 전송
    void publishReservationCompleted(Long userId, String resourceName);
}
