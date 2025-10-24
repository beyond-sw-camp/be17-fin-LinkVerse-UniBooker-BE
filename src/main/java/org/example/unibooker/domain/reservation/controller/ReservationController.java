package org.example.unibooker.domain.reservation.controller;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.reservation.model.dto.ReservationDto;
import org.example.unibooker.domain.reservation.service.ReservationService;
import org.example.unibooker.domain.resource.model.ServiceCategory;
import org.example.unibooker.domain.user.model.dto.AuthDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            Long userId) {
        /**
         * TODO : 로그인 로직 구현 되면 param으로 id 받은거 지우고, 인증 유저로 변경
         * @AuthenticationPrincipal AuthDto.AuthUser authUser
         * */
        return ResponseEntity.ok(BaseResponse.success(reservationService.reserve(dto, resourceId, userId)));
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
    // 예약 목록 조회- 일반 사용자
    // ===================
    @Operation(summary = "일반 사용자 예약 목록 조회", description = "일반 사용자가 예약/신청에 대한 목록 조회를 합니다.")
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<ReservationDto.UserResponseList>> getUserReservations(Long userId) {
        /**
         * TODO : 로그인 로직 구현 되면 param으로 id 받은거 지우고, 인증 유저로 변경
         * @AuthenticationPrincipal AuthDto.AuthUser authUser
         * */
        return ResponseEntity.ok(BaseResponse.success(reservationService.getUserReservations(userId)));
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
    public ResponseEntity<BaseResponse<String>> deleteReservation(@PathVariable Long reservationId) {
        reservationService.cancel(reservationId);
        return ResponseEntity.ok(BaseResponse.success("예약이 취소 되었습니다."));
    }
}
