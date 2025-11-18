package org.example.apireservation.domain.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.example.apireservation.domain.model.entity.Reservations;
import org.example.apireservation.domain.model.User;
import org.example.apireservation.domain.model.ServiceCategory;
import org.example.apireservation.domain.model.entity.Users;
import org.example.apireservation.infrastructure.ResourceFeignAdapter;
import org.example.apireservation.domain.model.Resource;
import org.example.apireservation.mapper.UserMapper;
import org.example.apireservation.usecase.port.in.ReservationCommand;
import org.example.apireservation.usecase.port.in.ReservationTrendCommand;
import org.example.apireservation.usecase.port.out.ReservationPersistencePort;
import org.example.apireservation.usecase.port.out.UserPersistencePort;
import org.example.common.base.BaseResponse;
import org.example.common.base.BaseResponseStatus;
import org.example.common.exception.BaseException;
import org.example.common.model.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 도메인 검증 서비스
 * DB는 ReservationPersistencePort을 통해 접근 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationPersistencePort reservationPersistencePort;
    private final ResourceFeignAdapter resourceFeignAdapter;
    private final UserPersistencePort userPersistencePort;


    // ========================== 사용자 검증 ==========================
    public User validateUser(Long userId) {
        // 사용자 상세 조회 (외부 호출)
        Users entity = userPersistencePort.findById(userId).orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));
        User user = UserMapper.from(entity); // entity -> domain

        if (user == null || !(user.getRole() == UserRole.USER)) {
            throw new BaseException(BaseResponseStatus.INVALID_USER_ROLE);
        }

        return user;
    }


    // ========================== 리소스 검증 ==========================
    public Resource validateResource(Long resourceId) {
        BaseResponse<Resource> response;

        try {
            response = resourceFeignAdapter.findResourceByIdForUpdate(resourceId);
        } catch (FeignException.NotFound e) {
            throw new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND);
        }

        Resource resource = response.getData();

        if (resource == null) {
            throw new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND);
        }

        return resource;
    }


    // ========================== 예약일 날짜 변환 ==========================
    public LocalDateTime[] transDate(Resource resource, ReservationCommand dto) {

        LocalDateTime startDate, endDate;

        // 예약일 변환. 신청은 날짜랑 시간 예약이 없음. 신청일은 createdAt 으로 구별
        if(!resource.getCategory().equals(ServiceCategory.EVENT)) {
            startDate = dto.getDate().atTime(dto.getTime());
            endDate = startDate.plusMinutes(resource.getTimeInterval());
        } else {
            startDate = null; endDate = null;
        }

        return new LocalDateTime[]{startDate, endDate};
    }


    // ========================== 중복 예약 체크 (사용자 입장) ==========================
    public void duplicatedReservationCheck(Resource resource, User user, LocalDateTime[] dates, ReservationCommand dto) {

        List<Reservations> duplicatedReservations = switch (resource.getCategory()) {
            case RESERVATION -> reservationPersistencePort.findDuplicatedReservation(user.getId(), resource.getId(), dates[0], dates[1]);
            case SEAT -> reservationPersistencePort.findDuplicatedReservationSeat(user.getId(), resource.getId(), dates[0], dates[1], dto.getRow(), dto.getCol());
//            case EVENT -> reservationPersistencePort.findDuplicatedReservation(user.getId(), resource.getId(), resource.getStartDate().atStartOfDay(), resource.getEndDate().atStartOfDay());
            case EVENT -> {
                // 상시 모집 체크
                if (Boolean.TRUE.equals(resource.getIsAlwaysAvailable())) {
                    yield reservationPersistencePort.findDuplicatedReservation(user.getId(), resource.getId());
                }
                yield reservationPersistencePort.findDuplicatedReservation(user.getId(), resource.getId(), resource.getStartDate().atStartOfDay(), resource.getEndDate().atStartOfDay());
            }
            default -> throw new BaseException(BaseResponseStatus.INVALID_SERVICE_CATEGORY);
        };

        if (!duplicatedReservations.isEmpty()) {
            throw new BaseException(BaseResponseStatus.RESERVATION_DUPLICATED);
        }
    }


    // ========================== 정원 초과 체크 (리소스 입장) ==========================
    public void overCapacityCheck(Resource resource, LocalDateTime[] dates, ReservationCommand dto) {

        if(resource.getCategory().equals(ServiceCategory.SEAT)) { // 요일 별 설정 수용인원 만큼 해당 시간대에 수용 가능
            Integer currentCount = reservationPersistencePort.countBySeatReservation(resource.getId(), dates[0], dates[1], dto.getRow(), dto.getCol()).size();
            if (currentCount > 0 || currentCount+dto.getHeadCount() >= resource.getCapacity() || dto.getHeadCount() > 1) {
                throw new BaseException(BaseResponseStatus.RESOURCE_OVER_CAPACITY);
            }
        } else if(resource.getCategory().equals(ServiceCategory.RESERVATION)) { // 시간대별 한 타임 예약 가능
            Integer currentCount = reservationPersistencePort.countByReservation(resource.getId(), dates[0], dates[1]).size();
            if (currentCount > 0 || dto.getHeadCount() >  resource.getCapacity()) {
                throw new BaseException(BaseResponseStatus.RESOURCE_OVER_CAPACITY);
            }
        } else if(resource.getCategory().equals(ServiceCategory.EVENT)) { // 수용인원 만큼 수용 가능
            Integer currentCount = reservationPersistencePort.countByResourceIdAndDeletedAtIsNull(resource.getId());
            if(currentCount+dto.getHeadCount() >= resource.getCapacity()) {
                throw new BaseException(BaseResponseStatus.RESOURCE_OVER_CAPACITY);
            }
        }
    }
}