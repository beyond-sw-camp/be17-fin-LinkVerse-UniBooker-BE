package org.example.unibooker.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.reservation.model.dto.ReservationDto;
import org.example.unibooker.domain.reservation.model.entity.Reservations;
import org.example.unibooker.domain.reservation.repository.ReservationRepository;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final CompanyRepository companyRepository;

    /**
     * 예약하기
     */
    public ReservationDto.Response reserve(ReservationDto.Request dto, Long resourceId, Long userId) {
        // 리소스 존재 여부 체크
        Resources resource = resourceRepository.findById(resourceId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));

        // entity로 변환 시 예약 중복, 정원 초콰, 기간 내 예약 가능한 리소스인지 등 체크
        Reservations result = reservationRepository.save(dto.toEntity(userId, resource));

        return ReservationDto.Response.from(result);
    }

    /**
     * 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자
     * */
    public void getAdminReservations(Long companyId, Long resourceId) {
        // TODO : 예약 목록 조회 서비스 구현 - 예약과 리소스 테이블 조인
        companyRepository.findById(companyId).orElseThrow(() -> new BaseException(BaseResponseStatus.COMPANY_NOT_FOUND));
        resourceRepository.findById(resourceId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));
    }

    /**
     * 예약 목록 조회 - 일반 사용자
     * */
    public ReservationDto.ResponseList getUserReservations(Long userId) {
        List<Reservations> result = reservationRepository.findAllByUsers_Id(userId);
        return ReservationDto.ResponseList.from(result);
    }

    /**
     * 예약 상세 조회
     */
    public ReservationDto.Response getReservationDetail(Long reservationId) {
        Reservations result = reservationRepository.findById(reservationId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESERVATION_NOT_FOUND));
        return ReservationDto.Response.from(result);
    }

    /**
     * 예약 취소
     */
    public void cancel(Long reservationId) {
        // TODO : 예약이 수정(수정은 취소로 간주)된 적 있는지도 확인 필요, 리소스도 마감된 것을 풀어줘야 함
        //예약 내역 존재 여부 체크
        Reservations reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESERVATION_NOT_FOUND));

        // 예약한 내역이 있으면 예약 상태 수정
        reservation.cancel();

        try {
            reservationRepository.save(reservation);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.RESERVATION_CANCEL_FAILED);
        }
    }
}