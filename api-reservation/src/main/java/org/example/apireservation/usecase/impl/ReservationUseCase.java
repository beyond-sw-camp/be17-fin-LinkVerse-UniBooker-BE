package org.example.apireservation.usecase.impl;

import feign.FeignException;
import lombok.*;
import org.example.apireservation.domain.model.dto.CustomFieldValueDto;
import org.example.apireservation.domain.model.dto.ReservationDetailDto;
import org.example.apireservation.domain.model.dto.ReservationListDto;
import org.example.apireservation.domain.model.entity.Reservations;
import org.example.apireservation.domain.model.*;
import org.example.apireservation.domain.model.entity.Users;
import org.example.apireservation.domain.service.*;
import org.example.apireservation.infrastructure.*;
import org.example.apireservation.mapper.CustomFieldValueMapper;
import org.example.apireservation.mapper.ReservationMapper;
import org.example.apireservation.mapper.UserMapper;
import org.example.apireservation.usecase.port.in.*;
import org.example.apireservation.usecase.port.out.*;
import org.example.common.base.BaseResponse;
import org.example.common.base.BaseResponseStatus;
import org.example.common.exception.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 실제 유즈케이스 구현체
 * Service 비즈니스 로직 수행 */
@Service
@RequiredArgsConstructor
public class ReservationUseCase implements ReservationWebPort {

    private final ReservationPersistencePort reservationPersistencePort;
    private final UserPersistencePort userPersistencePort;

    private final ResourceFeignAdapter resourceFeignAdapter;                    // 리소스 외부 API 호출
    private final ResourceGroupFeignAdapter resourceGroupFeignAdapter;          // 리소스 그룹 외부 API 호출
    private final CustomFieldValueFeignAdapter customFieldValueFeignAdapter;    // 사용자 커스텀 입력 필드 외부 API 호출

    private final ReservationService reservationService;                        // Domain Validator


    // ========================== 예약 요청 ==========================
    @Override
    @Transactional
    public ReservationDetailDto.Response reserve(ReservationCommand dto, Long resourceId, Long userId) {

        // 도메인 검증 및 생성
        Reservation domain = Reservation.toDomain(dto, resourceId, userId, reservationService); // command -> entity

        // 예약 생성 및 저장
        Reservations savedReservation = reservationPersistencePort.save(ReservationMapper.toEntity(domain));
        Reservation reservation = ReservationMapper.from(savedReservation, domain); // entity -> domain


        // 사용자 커스텀 필드 값 저장 (외부 호출)
        List<CustomFieldValueDto> userCustomFieldValuesDto = null;
        if(dto.getCustomFieldValues() != null && !dto.getCustomFieldValues().isEmpty()) {
            List<CustomFieldValue> userCustomFieldValues = customFieldValueFeignAdapter.register(reservation.getId(), dto.getCustomFieldValues());
            userCustomFieldValuesDto = userCustomFieldValues.stream().map(CustomFieldValueMapper::toDto).toList();
        }

        // TODO: 예약 확정 알림 발송 (외부 호출)

        // 카테고리 별 알맞은 형식으로 응답
        return ReservationMapper.toRes(reservation, userCustomFieldValuesDto); // domain -> dto;
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
}
