package org.example.unibooker.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.company.repository.CompanyRepository;
import org.example.unibooker.domain.reservation.model.dto.ReservationDto;
import org.example.unibooker.domain.reservation.model.entity.ReservationStatus;
import org.example.unibooker.domain.reservation.model.entity.Reservations;
import org.example.unibooker.domain.reservation.repository.ReservationRepository;
import org.example.unibooker.domain.resource.model.ResourceStatus;
import org.example.unibooker.domain.resource.model.Resources;
import org.example.unibooker.domain.resource.repository.ResourceGroupRepository;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final ResourceRepository resourceRepository;

    /**
     * 예약하기
     */
    public ReservationDto.Response reserve(ReservationDto.Request dto, Long resourceId, Long userId) {
        // 리소스 존재 여부 체크
        Resources resource = resourceRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));

        // entity로 변환 시 예약 중복, 정원 초과, 기간 내 예약 가능한 리소스인지 등 체크
        Reservations result = reservationRepository.save(dto.toEntity(userId, resource));

        return ReservationDto.Response.from(result);
    }

    /**
     * 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자 "리소스 그룹"의 목록
     * */
    public ReservationDto.ResponseList getAdminResourceGroupReservations(Long resourceGroupId) {
        // 리소스 그룹 존재 여부 체크
        resourceGroupRepository.findById(resourceGroupId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_GROUP_NOT_FOUND));

        // 리소스 그룹 별 전체 예약 목록 조회
        List<Reservations> result = reservationRepository.findAllByResourceGroupIdWithReservation(resourceGroupId);
        return ReservationDto.ResponseList.from(result);
    }


    /**
     * 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자 "리소스"의 목록
     */
    public ReservationDto.ResponseList getAdminResourceReservations(Long resourceId) {
        // 리소스 존재 여부 체크
        resourceRepository.findById(resourceId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));

        // 리소스 별 전체 예약 목록 조회
        List<Reservations> result = reservationRepository.findAllByResourceIdWithReservation(resourceId);
        return ReservationDto.ResponseList.from(result);
    }


    /**
     * 예약 목록 조회 - 일반 사용자
     * */
    public ReservationDto.ResponseList getUserReservations(Long userId) {
        List<Reservations> result = reservationRepository.findAllByUsersId(userId);
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
        //예약 내역 존재 여부 체크
        Reservations reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESERVATION_NOT_FOUND));

        // 예약이 취소된 적 있는지 체크
        if(reservation.getDeletedAt() != null && reservation.getStatus() != ReservationStatus.CANCELLED) {
            throw new BaseException(BaseResponseStatus.RESERVATION_ALREADY_CANCELED);
        }

        // 예약 상태 수정
        reservation.cancel();

        // TODO : 리소스도 마감된 것을 풀어줘야 함
        resourceStatusUpdate(reservation.getResources().getId());

        // 예상치 못한 경우 취소 실패하는 경우 예외처리
        try {
            reservationRepository.save(reservation);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.RESERVATION_CANCEL_FAILED);
        }
    }

    // TODO : 리소스도 마감된 것을 풀어줘야 함 > 리소스 서비스 클래스에 구현
    public void resourceStatusUpdate(Long resourceId) {
        // 리소스 존재 여부 체크
        Resources resource = resourceRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));

        // TODO : 리소스가 마감이면 상태 변경

        resource.setUpdateStatus(ResourceStatus.IN_PROGRESS); // 여기서 수정자는 일반 사용자로 해야하는가?

    }
}