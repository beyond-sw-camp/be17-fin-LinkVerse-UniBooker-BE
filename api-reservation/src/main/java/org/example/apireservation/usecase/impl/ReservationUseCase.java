package org.example.apireservation.usecase.impl;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.example.apireservation.domain.model.dto.*;
import org.example.apireservation.domain.model.entity.Reservations;
import org.example.apireservation.domain.model.*;
import org.example.apireservation.domain.model.entity.Users;
import org.example.apireservation.domain.service.*;
import org.example.apireservation.infrastructure.*;
import org.example.apireservation.lock.config.LockKeyGenerator;
import org.example.apireservation.mapper.CustomFieldValueMapper;
import org.example.apireservation.mapper.ReservationMapper;
import org.example.apireservation.mapper.UserMapper;
import org.example.apireservation.usecase.port.in.*;
import org.example.apireservation.usecase.port.out.*;
import org.example.common.base.BaseResponse;
import org.example.common.base.BaseResponseStatus;
import org.example.common.exception.BaseException;
import org.example.common.model.UserRole;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 실제 유즈케이스 구현체
 * Service 비즈니스 로직 수행 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationUseCase implements ReservationWebPort {

    private final ReservationPersistencePort reservationPersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final ReservationKafkaPort reservationKafkaPort;

    /** api 호출 */
    private final ResourceFeignAdapter resourceFeignAdapter;                    // 리소스 API 호출
    private final ResourceGroupFeignAdapter resourceGroupFeignAdapter;          // 리소스 그룹 API 호출
    private final CustomFieldValueFeignAdapter customFieldValueFeignAdapter;    // 사용자 커스텀 입력 필드 API 호출

    /** 도메인 서비스 */
    private final ReservationService reservationService;                        // Domain Validator

    /** 락 관련 */
    private final RedissonClient redisson;                                      // Redisson 분산락


    // ========================== 예약 요청 ==========================
    @Override
    @Transactional
    public ReservationDetailDto.Response reserve(ReservationCommand dto, Long resourceId, Long userId, Long companyId) {

        // 리소스 조회 (락 키 생성하는데 필요)
        Resource resource = reservationService.validateResource(resourceId);
        ServiceCategory category = resource.getCategory();

        // 락 키 생성
        String lockKey = LockKeyGenerator.buildLockKey(category, resourceId, dto);
        RLock lock = redisson.getLock(lockKey);

        try {
            lock.lock();

            // 락 획득 후 도메인 검증 및 생성
            Reservation domain = Reservation.toDomain(dto, resourceId, userId, companyId, reservationService);

            // 예약 생성 및 저장
            Reservations savedReservation = reservationPersistencePort.save(ReservationMapper.toEntity(domain));
            Reservation reservation = ReservationMapper.from(savedReservation, domain); // entity -> domain

            // 사용자 커스텀 필드 값 저장 (내부 호출)
            List<CustomFieldValueDto> userCustomFieldValuesDto = null;
            if (dto.getCustomFieldValues() != null && !dto.getCustomFieldValues().isEmpty()) {
                try {
                    List<CustomFieldValue> userCustomFieldValues = customFieldValueFeignAdapter.register(reservation.getId(), dto.getCustomFieldValues());
                    userCustomFieldValuesDto = userCustomFieldValues.stream().map(CustomFieldValueMapper::toDto).toList();
                } catch (Exception fe) {
                    // TODO : 내부 호출 실패 시 예약 롤백
                    log.warn("[reserve] custom field registration failed for reservation {}: {}", reservation.getId(), fe.getMessage());
                }
            }

            reservationKafkaPort.publishReservationCompleted(userId, resource.getName());

            return ReservationMapper.toRes(reservation, userCustomFieldValuesDto); // domain -> dto
        }

        finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


    // ========================== 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자 "리소스 그룹"의 목록 ==========================
    @Override
    public ReservationListDto.ResponseList getAdminReservations(Long resourceGroupId) {

        // 리소스 그룹 존재 여부 체크 (외부 호출)
        ResourceGroup resourceGroups = resourceGroupFeignAdapter.findByIdAndDeletedAtIsNull(resourceGroupId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESOURCE_GROUP_NOT_FOUND));

        // 리소스 그룹 별 전체 예약 목록 조회
        List<Reservations> result = reservationPersistencePort.findAllByResourceGroupIdWithReservation(resourceGroupId);

        List<Reservation> reservations = result.stream().map(entity -> Reservation.toDomain(entity, entity.getResourceId(), entity.getUserId(), reservationService)).collect(Collectors.toList()); // entity -> domain
        return ReservationMapper.toRes(reservations, resourceGroups.getServiceCategory());
    }


    // ========================== 특정 서비스의 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자 ==========================
    @Override
    public ReservationListDto.ResponseList getResourceReservations(Long resourceId, LocalDateTime startDate, LocalDateTime endDate) {

        // 리소스 존재 여부 체크 (외부 호출)
        BaseResponse<Resource> response = resourceFeignAdapter.findById(resourceId);
        Resource resource = response.getData();
        if (resource == null) {
            throw new BaseException(BaseResponseStatus.RESOURCE_NOT_FOUND);
        }

        List<Reservations> result;

        if (startDate != null && endDate != null) {
            result = reservationPersistencePort.findAllByResourceIdAndStartDateBetween(resourceId, startDate, endDate);
        } else {
            result = reservationPersistencePort.findAllByResourceId(resourceId);
        }

        List<Reservation> reservations = result.stream().map(entity -> Reservation.toDomain(entity, entity.getResourceId(), entity.getUserId(), reservationService)).collect(Collectors.toList()); // entity -> domain

        return ReservationMapper.toRes(reservations, resource.getCategory());
    }


    // ========================== 예약 목록 조회- 일반 사용자 ==========================
    @Override
    public ReservationListDto.UserResponseList getUserReservations(Long userId) {

        // TODO : 취소된 예약은 안보이게 조회하는 코드로 수정
        List<Reservations> result = reservationPersistencePort.findAllByUserId(userId);
        List<Reservation> reservations = result.stream().map(entity -> Reservation.toDomain(entity, entity.getResourceId(), entity.getUserId(), reservationService)).collect(Collectors.toList()); // entity -> domain
        return ReservationMapper.toRes(reservations);
    }


    // ========================== 예약 상세 조회 ==========================
    @Override
    public ReservationDetailDto.Response getReservationDetail(Long reservationId) {
        // 예약 존재 여부 체크
        Reservations entity = reservationPersistencePort.findById(reservationId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESERVATION_NOT_FOUND));
        Reservation reservation = Reservation.toDomain(entity, entity.getResourceId(), entity.getUserId(), reservationService);

        // 하나의 예약에 대한 사용자 압력 커스텀 필드 값 리스트 조회 (외부 호출)
        BaseResponse<List<CustomFieldValue>> userCustomFieldValues = customFieldValueFeignAdapter.getUserFieldValuesByReservation(reservationId);
        List<CustomFieldValue> res = userCustomFieldValues.getData();
        List<CustomFieldValueDto> userCustomFieldValuesDto = res.stream().map(CustomFieldValueMapper::toDto).collect(Collectors.toList()); // domain -> dto

        // 카테고리 별 알맞은 형식으로 응답
        return ReservationMapper.toRes(reservation, userCustomFieldValuesDto);
    }


    // ========================== 예약 취소 ==========================
    @Override
    public void cancel(Long reservationId, Long userId) {
        // 사용자 존재 여부 체크
        Users entity = userPersistencePort.findById(userId).orElseThrow(() -> new BaseException(BaseResponseStatus.USER_NOT_FOUND));
        User user = UserMapper.from(entity);

        // 예약 내역 존재 여부 체크
        Reservations reservation = reservationPersistencePort.findByIdAndDeletedAtIsNull(reservationId).orElseThrow(() -> new BaseException(BaseResponseStatus.RESERVATION_NOT_FOUND));

        // 예약이 취소된 적 있는지 체크
        if(reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BaseException(BaseResponseStatus.RESERVATION_ALREADY_CANCELED);
        }

        // TODO : 취소하려는 예약이 사용자가 예약한 것인지 체크

        // 예약 상태 수정
        reservation.cancel();

        // TODO : 리소스도 마감된 것을 풀어줄 것인지

        // 예상치 못한 경우 취소 실패하는 경우 예외처리
        try {
            reservationPersistencePort.save(reservation);
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.RESERVATION_CANCEL_FAILED);
        }
    }


    // ========================== 특정 기업의 전체 예약 수 조회 ==========================
    @Override
    public Integer getAllReservationCountsByCompany(Long companyId) {
        return reservationPersistencePort.countByCompanyId(companyId);
    }


    // ========================== 특정 기간 동안의 리소스 그룹별 예약 수 조회 ==========================
    @Override
    public List<ReservationTrendDto> getReservationCountsByGroupResources(ReservationTrendCommand dto) {
        List<Object[]> result = new ArrayList<>();

        for(Long resourceGroupId:dto.getGroupIds()) {
            result.addAll(reservationPersistencePort.countReservationByGroupAndDate(resourceGroupId, dto.getTo(), dto.getFrom()));
        }

        return result.stream().map(ReservationMapper::toResGroupCountList).toList();
    }


    // ========================== 리소스 그룹의 누적 예약수 ==========================
    @Override
    public Integer getCumReservationCount(Long resourceGroupId) {
        return reservationPersistencePort.getCumReservationCount(resourceGroupId);
    }


    // ========================== 리소스 그룹의 누적 취소 예약 수==========================
    @Override
    public Integer getCumCancelCount(Long resourceGroupId) {
        return reservationPersistencePort.getCumCancelCount(resourceGroupId);
    }

    // ========================== 리소스 그룹에 속하는 리소스 수 ==========================
    @Override
    public List<ServiceGroupDashBoardDto.ServicePerformanceCount> getServicePerformanceCount(Long resourceGroupId) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        List<Object[]> result = reservationPersistencePort.getServicePerformanceCount(resourceGroupId, oneMonthAgo);

        return result.stream().map(ReservationMapper::toResReservationCountByGroupResource).toList();
    }

    // ========================== 리소스 그룹에 속하는 사용자 (중복제거) ==========================
    @Override
    public ServiceGroupDashBoardDto.VisitorCount getVisitorCount(Long resourceGroupId, Long companyId, UserRole userRole) {
        Integer total = userPersistencePort.getTotalUserCountWithCompanyId(companyId, userRole);
        Integer count = reservationPersistencePort.getReservationUserCount(resourceGroupId);
        return ReservationMapper.toResVisitorCount(total, count);
    }

    // ========================== 성별 ==========================
    @Override
    public List<ServiceGroupDashBoardDto.GenderReservationCount> getGenderReservationCount(Long resourceGroupId) {
        List<Object[]> result = reservationPersistencePort.getGenderReservationCount(resourceGroupId);

        return result.stream().map(ReservationMapper::toResGenderCount).toList();
    }

    // ========================== 나이대 ==========================
    @Override
    public List<ServiceGroupDashBoardDto.AgeReservationCount> getAgeReservationCount(Long resourceGroupId) {
        List<Object[]> result = reservationPersistencePort.getAgeReservationCount(resourceGroupId);

        return result.stream().map(ReservationMapper::toResAgeCount).toList();
    }

    // ========================== 리소스 그룹에 속하는 시간대 별 예약 수 ==========================
    @Override
    public List<ServiceGroupDashBoardDto.TimeSlotReservationCount> getTimeSlotReservationCount(Long resourceGroupId) {
        List<Object[]> result = reservationPersistencePort.getTimeSlotReservationCount(resourceGroupId);

        return result.stream().map(ReservationMapper::toResTimeSlotCount).toList();
    }


    @Override
    public List<ServiceGroupDashBoardDto.GroupReservationCountResponse> getCumReservationCountsByGroupResources(List<Long> groupIds) {
        // 빈 결과 리스트 초기화
        List<ServiceGroupDashBoardDto.GroupReservationCountResponse> result = new ArrayList<>();

        for (Long groupId : groupIds) {
            // groupId 기준으로 예약 수 조회
            int count = reservationPersistencePort.countByResourceGroupId(groupId);

            // DTO 생성 후 결과 리스트에 추가
            ServiceGroupDashBoardDto.GroupReservationCountResponse dto = ServiceGroupDashBoardDto.GroupReservationCountResponse.builder()
                    .groupId(groupId)
                    .count(count)
                    .build();

            result.add(dto);
        }

        return result;
    }
}