package org.example.unibooker.domain.reservation.controller;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.example.unibooker.common.BaseResponse;
import org.example.unibooker.domain.reservation.model.dto.ReservationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "예약 처리 기능", description = "예약 요청, 조회, 취소 등 예약 처리에 대한 전반적인 기능")
@RestController
@RequestMapping("/api/reservation")
public class ReservationController {
    // ===================
    // 예약 요청
    // ===================
    @Operation(
        summary = "예약 요청",
        description = "일반 사용자가 특정 기업의 사이트에서 예약/신청에 대한 요청을 합니다.",
        responses = {
                @ApiResponse(responseCode = "200", description = "예약 성공"),
                @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                @ApiResponse(responseCode = "401", description = "인증 필요"),
                @ApiResponse(responseCode = "403", description = "예약 요청 권한 없음"),
                @ApiResponse(responseCode = "409", description = "정원 초과 및 중복 예약 발생"),
                @ApiResponse(responseCode = "500", description = "서버 오류")
        }
    )
    @PostMapping("/{companyId}/{resourceId}")
    public ResponseEntity<BaseResponse<ReservationDto.Response>> createReservation(
            @RequestBody ReservationDto.Request request,
            @PathVariable Integer companyId,
            @PathVariable Integer resourceId) {

        // TODO : 예약 요청 로직 구현
        return null;
    }


    // ===================
    // 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자
    // ===================
    @Operation(
            summary = "예약 목록 조회",
            description = "플랫폼 관리자 및 기업 관리자가 특정 기업의 서비스마다 예약/신청에 대한 목록 조회를 합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 목록 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
            }
    )
    @GetMapping("/list/{companyId}/{resourceId}")
    public ResponseEntity<BaseResponse<List<ReservationDto.Response>>> getAdminReservations() {
        // TODO : 예약 목록 조회 로직 구현
        return null;
    }


    // ===================
    // 예약 목록 조회 - 일반 사용자
    // ===================
    @Operation(
            summary = "예약 목록 조회",
            description = "일반 사용자가 특정 기업의 예약/신청에 대한 목록 조회를 합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 목록 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
            }
    )
    @GetMapping("/list/{userId}")
    public ResponseEntity<BaseResponse<List<ReservationDto.Response>>> getUserReservations() {
        // TODO : 예약 목록 조회 로직 구현
        return null;
    }


    // ===================
    // 예약 상세 조회
    // ===================
    @Operation(
            summary = "예약 상세 조회",
            description = "모든 사용자가 특정 기업 서비스의 예약/신청에 대한 상세 조회를 합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 상세 조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "404", description = "특정 예약을 찾을 수 없음")
            }
    )
    @GetMapping("/detail/{resourceId}/{reservationId}")
    public ResponseEntity<BaseResponse<ReservationDto.Response>> getReservation() {
        // TODO : 예약 상세 조회 로직 구현
        return null;
    }


    // ===================
    // 예약 취소
    // ===================
    @Operation(
            summary = "예약 취소",
            description = "모든 사용자가 특정 기업의 서비스 예약/신청에 대한 예약 취소 요청을 합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 취소 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "403", description = "예약 취소 요청 권한 없음"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @DeleteMapping("/cancel/{resourceId}/{reservationId}")
    public ResponseEntity<BaseResponse<ReservationDto.Response>> deleteReservation() {
        // TODO : 예약 취소 로직 구현
        return null;
    }
}
