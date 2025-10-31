package org.example.unibooker.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.unibooker.common.BaseResponseStatus;
import org.example.unibooker.common.exception.BaseException;
import org.example.unibooker.domain.notification.model.NotificationType;
import org.example.unibooker.domain.notification.service.NotificationService;
import org.example.unibooker.domain.reservation.model.dto.ReservationDto;
import org.example.unibooker.domain.reservation.model.entity.ReservationStatus;
import org.example.unibooker.domain.reservation.model.entity.Reservations;
import org.example.unibooker.domain.reservation.repository.ReservationRepository;
import org.example.unibooker.domain.resource.model.*;
import org.example.unibooker.domain.resource.repository.ResourceGroupRepository;
import org.example.unibooker.domain.resource.repository.ResourceRepository;
import org.example.unibooker.domain.resource.service.CustomFieldValueService;
import org.example.unibooker.domain.user.model.UserRole;
import org.example.unibooker.domain.user.model.entity.Users;
import org.example.unibooker.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    /** service */
    private final CustomFieldValueService customFieldValueService;
    private final NotificationService notificationService;

    /** repository */
    private final ReservationRepository reservationRepository;
    private final ResourceGroupRepository resourceGroupRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    /**
     * 사용자 권한 확인
     */
    public void userRoleCheck(Users user) {
        if (user == null || !(user.getRole() == UserRole.USER)) {
            throw new BaseException(BaseResponseStatus.INVALID_USER_ROLE);
        }
    }

    /**
     * 예약하기
     */
    @Transactional
    public ReservationDto.Response reserve(ReservationDto.Request dto, Long resourceId, Long userId) {
        // 일반 사용자 체크
        Users user = userRepository.findById(userId).orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));
        userRoleCheck(user);

        // 리소스 존재 여부 체크
        Resources resource = resourceRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));

        // 예약 생성 및 저장
        Reservations reservation = reservationRepository.save(dto.toReservationEntity(user, resource, reservationRepository));

        // 사용자 커스텀 필드 값 저장
        List<Object> userCustomFieldValues = customFieldValueService.register(reservation.getId(), dto.getCustomFieldValues()); // 현재 받은 Object = UserCustomFieldValues
        List<CustomFieldDto.CustomFieldValueListRes> userCustomFieldValuesResult = userCustomFieldValues.stream().map(value -> CustomFieldDto.CustomFieldValueListRes.fromUserEntity((UserCustomFieldValues) value)).collect(Collectors.toList());

        // 예약 확정 알림 발송
        notificationService.sendNotificationToUser(
                NotificationType.RESERVATION_CONFIRMED,
                user,
                resource.getName()
        );

        // 카테고리 별 알맞은 형식으로 응답
        return switch (resource.getResourceGroup().getCategory()) {
            case RESERVATION -> ReservationDto.ReservationResponse.from(reservation, userCustomFieldValuesResult);
            case SEAT -> ReservationDto.SeatResponse.from(reservation, userCustomFieldValuesResult);
            case EVENT -> ReservationDto.EventResponse.from(reservation, userCustomFieldValuesResult);
            default -> throw new BaseException(BaseResponseStatus.INVALID_SERVICE_CATEGORY);
        };
    }


    /**
     * 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자
     * 리소스 그룹에 예약된 예약/신청된 목록
     * */
    public ReservationDto.ResponseList getAdminReservations(Long resourceGroupId) {
        // 리소스 그룹 존재 여부 체크
        ResourceGroups resourceGroups = resourceGroupRepository.findByIdAndDeletedAtIsNull(resourceGroupId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_GROUP_NOT_FOUND));

        // 리소스 그룹 별 전체 예약 목록 조회
        // TODO : 취소된 예약은 안보이게 조회하는 코드로 수정
        List<Reservations> result = reservationRepository.findAllByResourceGroupIdWithReservation(resourceGroupId);

        return ReservationDto.ResponseList.from(result, resourceGroups.getCategory());
    }

    /**
     * 특정 리소스의 예약 목록 조회
     */

    public ReservationDto.ResponseList getResourceReservations(Long resourceId, LocalDateTime startDate, LocalDateTime endDate) {
        Resources resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));

        List<Reservations> result;

        if (startDate != null && endDate != null) {
            result = reservationRepository.findAllByResourcesIdAndStartDateBetween(resourceId, startDate, endDate);
        } else {
            result = reservationRepository.findAllByResourcesId(resourceId);
        }

        return ReservationDto.ResponseList.from(result, resource.getResourceGroup().getCategory());

    }


    /**
     * 예약 목록 조회 - 일반 사용자
     * */
    public ReservationDto.UserResponseList getUserReservations(Long userId) {
        List<Reservations> result = reservationRepository.findAllByUsersId(userId);
        return ReservationDto.UserResponseList.from(result);
    }


    /**
     * 예약 상세 조회
     */
    public ReservationDto.Response getReservationDetail(Long reservationId) {
        // 예약 존재 여부 체크
        Reservations reservation = reservationRepository.findById(reservationId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESERVATION_NOT_FOUND));

        // 하나의 예약에 대한 사용자 압력 커스텀 필드 값 리스트
        List<CustomFieldDto.CustomFieldValueListRes> userCustomFieldValues = customFieldValueService.getUserFieldValuesByReservation(reservationId);

        // 카테고리 별 알맞은 형식으로 응답
        return switch (reservation.getResources().getResourceGroup().getCategory()) {
            case RESERVATION -> ReservationDto.ReservationResponse.from(reservation, userCustomFieldValues);
            case SEAT -> ReservationDto.SeatResponse.from(reservation, userCustomFieldValues);
            case EVENT -> ReservationDto.EventResponse.from(reservation, userCustomFieldValues);
            default -> throw new BaseException(BaseResponseStatus.INVALID_SERVICE_CATEGORY);
        };
    }


    /**
     * 예약 취소
     */
    public void cancel(Long reservationId, Long userId) {
        // 사용자 존재 여부 체크
        Users user = userRepository.findById(userId).orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        // 예약 내역 존재 여부 체크
        Reservations reservation = reservationRepository.findByIdAndDeletedAtIsNull(reservationId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESERVATION_NOT_FOUND));

        // 예약이 취소된 적 있는지 체크
        if(reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BaseException(BaseResponseStatus.RESERVATION_ALREADY_CANCELED);
        }

        /*
        // TODO : 취소하려는 예약이 사용자가 예약한 것인지 체크
        if(user.getRole().equals(UserRole.USER)) {

        }
        */

        // 예약 상태 수정
        reservation.cancel();

        // TODO : 리소스도 마감된 것을 풀어줄 것인지
        // resourceStatusUpdate(reservation.getResources().getId());

        // 예상치 못한 경우 취소 실패하는 경우 예외처리
        try {
            reservationRepository.save(reservation);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.RESERVATION_CANCEL_FAILED);
        }
    }

    // TODO : 리소스도 마감된 것 상태 변경 > 리소스 서비스 클래스에 구현
    public void resourceStatusUpdate(Long resourceId) {
        // 리소스 존재 여부 체크
        Resources resource = resourceRepository.findByIdAndIsActiveTrueAndDeletedAtIsNull(resourceId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));

        // TODO : 리소스가 마감이면 상태 변경

        resource.setStatus(ResourceStatus.IN_PROGRESS); // 여기서 수정자는 일반 사용자로 해야하는가?

    }
}