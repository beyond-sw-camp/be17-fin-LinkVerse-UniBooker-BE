package org.example.apireservation.domain.service;

import lombok.RequiredArgsConstructor;
import org.example.apireservation.adapter.out.Reservations;
import org.example.apireservation.adapter.out.external.UserInfo;
import org.example.apireservation.domain.model.ServiceCategory;
import org.example.apireservation.infrastructure.ResourceExternalPort;
import org.example.apireservation.adapter.out.external.ResourceInfo;
import org.example.apireservation.infrastructure.UserExternalPort;
import org.example.apireservation.usecase.port.in.ReservationCommand;
import org.example.apireservation.usecase.port.out.ReservationPersistencePort;
import org.example.common.base.BaseResponseStatus;
import org.example.common.exception.BaseException;
import org.example.common.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 도메인 검증 서비스
 * DB는 ReservationPersistencePort을 통해 접근 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationPersistencePort reservationPersistencePort;
    private final ResourceExternalPort resourceExternalPort;
    private final UserExternalPort userExternalPort;


    // ========================== 사용자 검증 ==========================
    public void validateUser(Long userId) {
        UserInfo user = userExternalPort.findUserById(userId).orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        if (user == null || !(user.getRole() == UserRole.USER)) {
            throw new BaseException(BaseResponseStatus.INVALID_USER_ROLE);
        }
    }


    // ========================== 리소스 검증 ==========================
    public void validateResource(Long resourceId) {
        resourceExternalPort.findResourceByIdForUpdate(resourceId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));
    }


    // ========================== 예약일 날짜 변환 ==========================
    public LocalDateTime[] transDate(Long resourceId, ReservationCommand dto) {

        ResourceInfo resource = resourceExternalPort.findResourceById(resourceId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));
        LocalDateTime startDate, endDate;

        // 예약일 변환. 신청은 날짜랑 시간 예약이 없음. 신청일은 createdAt 으로 구별
        if(!resource.getServiceCategory().equals(ServiceCategory.EVENT)) {
            startDate = dto.getDate().atTime(dto.getTime());
            endDate = startDate.plusMinutes(resource.getTimeInterval());
        } else {
            startDate = null; endDate = null;
        }

        return new LocalDateTime[]{startDate, endDate};
    }


    // ========================== 중복 예약 체크 (사용자 입장) ==========================
    public void duplicatedReservationCheck(Long resourceId, Long userId, LocalDateTime[] dates, ReservationCommand dto) {
        ResourceInfo resource = resourceExternalPort.findResourceById(resourceId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));
        UserInfo user = userExternalPort.findUserById(userId).orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));

        List<Reservations> duplicatedReservations = switch (resource.getServiceCategory()) {
            case RESERVATION -> reservationPersistencePort.findDuplicatedReservation(user.getId(), resource.getId(), dates[0], dates[1]);
            case SEAT -> reservationPersistencePort.findDuplicatedReservationSeat(user.getId(), resource.getId(), dates[0], dates[1], dto.getRow(), dto.getCol());
            case EVENT -> reservationPersistencePort.findDuplicatedReservation(user.getId(), resource.getId(), resource.getStartDate().atStartOfDay(), resource.getEndDate().atStartOfDay());
            default -> throw new BaseException(BaseResponseStatus.INVALID_SERVICE_CATEGORY);
        };

        if (!duplicatedReservations.isEmpty()) {
            throw new BaseException(BaseResponseStatus.RESERVATION_DUPLICATED);
        }
    }


    // ========================== 정원 초과 체크 (리소스 입장) ==========================
    public void overCapacityCheck(Long resourceId, LocalDateTime[] dates, ReservationCommand dto) {
        ResourceInfo resource = resourceExternalPort.findResourceById(resourceId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND));

        if(resource.getServiceCategory().equals(ServiceCategory.SEAT)) { // 요일 별 설정 수용인원 만큼 해당 시간대에 수용 가능
            Integer currentCount = reservationPersistencePort.countBySeatReservation(resource.getId(), dates[0], dates[1], dto.getRow(), dto.getCol()).size();
            if (currentCount+dto.getHeadCount() >= resource.getCapacity() || dto.getHeadCount() > 1) {
                throw new BaseException(BaseResponseStatus.RESOURCE_OVER_CAPACITY);
            }
        } else if(resource.getServiceCategory().equals(ServiceCategory.RESERVATION)) { // 시간대별 한 타임 예약 가능
            Integer currentCount = reservationPersistencePort.countByReservation(resource.getId(), dates[0], dates[1]).size();
            if (currentCount > 0 || dto.getHeadCount() >  resource.getCapacity()) {
                throw new BaseException(BaseResponseStatus.RESOURCE_OVER_CAPACITY);
            }
        } else if(resource.getServiceCategory().equals(ServiceCategory.EVENT)) { // 수용인원 만큼 수용 가능
            Integer currentCount = reservationPersistencePort.countByResourcesIdAndDeletedAtIsNull(resource.getId()).size();
            if(currentCount+dto.getHeadCount() >= resource.getCapacity()) {
                throw new BaseException(BaseResponseStatus.RESOURCE_OVER_CAPACITY);
            }
        }
    }
}
