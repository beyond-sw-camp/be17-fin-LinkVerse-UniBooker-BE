package org.example.unibooker.domain.reservation.controller;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.reservation.model.dto.ReservationDto;
import org.example.unibooker.domain.reservation.service.ReservationService;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@Tag(name = "예약 처리 기능", description = "예약 요청, 조회, 취소 등 예약 처리에 대한 전반적인 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    // ===================
    // 예약 요청
    // ===================
    @Operation(summary = "예약 요청", description = "일반 사용자가 특정 기업의 사이트에서 예약/신청에 대한 요청을 합니다.")
    @PostMapping("/{resourceId}")
    public ResponseEntity<BaseResponse<ReservationDto.Response>> createReservation(
            @Valid @RequestBody ReservationDto.Request dto,
            @PathVariable Long resourceId,
            @AuthenticationPrincipal AuthDto.AuthUser authUser) {
        ReservationDto.Response response;
        response = reservationService.reserve(dto, resourceId, authUser.getId());
        return ResponseEntity.ok(BaseResponse.success(response));
    }


    // ===================
    // 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자 "리소스 그룹"의 목록
    // ===================
    @Operation(summary = "플랫폼 관리자 및 기업 관리자 예약 목록 조회", description = "플랫폼 관리자 및 기업 관리자가 리소스 그룹에 예약된 모든 예약/신청된 목록 조회를 합니다.")
    @GetMapping("/list/all/{resourceGroupId}")
    public ResponseEntity getAdminReservations(@PathVariable Long resourceGroupId) {
        return ResponseEntity.ok(BaseResponse.success(reservationService.getAdminReservations(resourceGroupId)));
    }

    // ===================
    // 특정 서비스의 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자
    // ===================
    @Operation(summary = "특정 서비스의 예약 목록 조회", description = "플랫폼 관리자 및 기업 관리자가 특정 리소스에 대한 예약/신청된 목록 조회를 합니다.")
    @GetMapping("/list/{resourceId}")
    public BaseResponse<ReservationDto.ResponseList> getResourceReservations(
            @PathVariable Long resourceId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {
        ReservationDto.ResponseList result = reservationService.getResourceReservations(resourceId, startDate, endDate);
        return BaseResponse.success(result);
    }


    // ===================
    // 예약 목록 조회- 일반 사용자
    // ===================
    @Operation(summary = "일반 사용자 예약 목록 조회", description = "일반 사용자가 예약/신청에 대한 목록 조회를 합니다.")
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<ReservationDto.UserResponseList>> getUserReservations(@AuthenticationPrincipal AuthDto.AuthUser authUser) {
        return ResponseEntity.ok(BaseResponse.success(reservationService.getUserReservations(authUser.getId())));
    }


    // ===================
    // 예약 상세 조회
    // ===================
    @Operation(summary = "예약 상세 조회", description = "모든 사용자가 특정 기업 서비스의 예약/신청에 대한 상세 조회를 합니다.")
    @GetMapping("/detail/{reservationId}")
    public ResponseEntity<BaseResponse<ReservationDto.Response>> getReservationDetail(@PathVariable Long reservationId) {
        return ResponseEntity.ok(BaseResponse.success(reservationService.getReservationDetail(reservationId)));
    }


    // ===================
    // 예약 취소
    // ===================
    @Operation(summary = "예약 취소", description = "모든 사용자가 특정 기업의 서비스 예약/신청에 대한 예약 취소 요청을 합니다.")
    @DeleteMapping("/cancel/{reservationId}")
    public ResponseEntity<BaseResponse<String>> deleteReservation(
            @PathVariable Long reservationId,
            @AuthenticationPrincipal AuthDto.AuthUser authUser) {
        reservationService.cancel(reservationId, authUser.getId());
        return ResponseEntity.ok(BaseResponse.success("예약이 취소 되었습니다."));
    }
}
