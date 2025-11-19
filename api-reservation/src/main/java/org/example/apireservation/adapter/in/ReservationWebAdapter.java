package org.example.apireservation.adapter.in;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.apireservation.domain.model.dto.ReservationDetailDto;
import org.example.apireservation.domain.model.dto.ReservationListDto;
import org.example.apireservation.domain.model.dto.ReservationTrendDto;
import org.example.apireservation.domain.model.dto.ServiceGroupDashBoardDto;
import org.example.apireservation.usecase.port.in.*;
import org.example.common.base.BaseResponse;
import org.example.common.model.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 예약 처리 컨트롤러 (헥사고날 아키텍처 - Web Adapter)
 * - 예약 요청, 조회, 취소
 * - 관리자용 예약 목록 및 통계 조회
 * - 대시보드용 예약 데이터 제공
 */
@Slf4j
@Tag(name = "Reservation API", description = "예약 처리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationWebAdapter {

    /** 예약 웹 포트 (Use Case 인터페이스) */
    private final ReservationWebPort reservationWebPort;

    /**
     * 예약 요청
     */
    @Operation(
            summary = "예약 요청",
            description = "일반 사용자가 특정 리소스에 대한 예약/신청 요청을 합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "예약 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "409", description = "예약 불가 (중복, 정원 초과 등)")
            }
    )
    @PostMapping("/{resourceId}")
    public ResponseEntity<BaseResponse<ReservationDetailDto.Response>> createReservation(
            @Valid @RequestBody ReservationCommand dto,

            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId,

            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId,

            @Parameter(description = "기업 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-Company-Id") Long companyId) {

        log.info("예약 요청 - resourceId: {}, userId: {}, companyId: {}", resourceId, userId, companyId);
        ReservationDetailDto.Response response = reservationWebPort.reserve(dto, resourceId, userId, companyId);
        return ResponseEntity.ok(BaseResponse.success(response));
    }

    /**
     * 관리자용 예약 목록 조회 (리소스 그룹별)
     */
    @Operation(
            summary = "관리자용 예약 목록 조회",
            description = "플랫폼 관리자 및 기업 관리자가 리소스 그룹에 속한 모든 예약 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @GetMapping("/list/all/{resourceGroupId}")
    public ResponseEntity<BaseResponse<ReservationListDto.ResponseList>> getAdminReservations(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        log.info("관리자용 예약 목록 조회 - resourceGroupId: {}", resourceGroupId);
        return ResponseEntity.ok(BaseResponse.success(reservationWebPort.getAdminReservations(resourceGroupId)));
    }

    /**
     * 특정 리소스의 예약 목록 조회
     */
    @Operation(
            summary = "특정 리소스의 예약 목록 조회",
            description = "플랫폼 관리자 및 기업 관리자가 특정 리소스에 대한 예약 목록을 기간별로 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @GetMapping("/list/{resourceId}")
    public BaseResponse<ReservationListDto.ResponseList> getResourceReservations(
            @Parameter(description = "리소스 ID", required = true, example = "1")
            @PathVariable Long resourceId,

            @Parameter(description = "조회 시작 일시", example = "2025-01-01T00:00:00")
            @RequestParam(required = false) LocalDateTime startDate,

            @Parameter(description = "조회 종료 일시", example = "2025-01-31T23:59:59")
            @RequestParam(required = false) LocalDateTime endDate) {

        log.info("리소스별 예약 목록 조회 - resourceId: {}, period: {} ~ {}", resourceId, startDate, endDate);
        ReservationListDto.ResponseList result = reservationWebPort.getResourceReservations(resourceId, startDate, endDate);
        return BaseResponse.success(result);
    }

    /**
     * 일반 사용자 예약 목록 조회
     */
    @Operation(
            summary = "일반 사용자 예약 목록 조회",
            description = "일반 사용자가 본인의 예약/신청 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<ReservationListDto.UserResponseList>> getUserReservations(
            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        log.info("일반 사용자 예약 목록 조회 - userId: {}", userId);
        return ResponseEntity.ok(BaseResponse.success(reservationWebPort.getUserReservations(userId)));
    }

    /**
     * 예약 상세 조회
     */
    @Operation(
            summary = "예약 상세 조회",
            description = "모든 사용자가 특정 예약/신청의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
            }
    )
    @GetMapping("/detail/{reservationId}")
    public ResponseEntity<BaseResponse<ReservationDetailDto.Response>> getReservationDetail(
            @Parameter(description = "예약 ID", required = true, example = "1")
            @PathVariable Long reservationId) {

        log.info("예약 상세 조회 - reservationId: {}", reservationId);
        return ResponseEntity.ok(BaseResponse.success(reservationWebPort.getReservationDetail(reservationId)));
    }

    /**
     * 예약 취소
     */
    @Operation(
            summary = "예약 취소",
            description = "모든 사용자가 예약/신청을 취소합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "취소 성공"),
                    @ApiResponse(responseCode = "400", description = "취소 불가 (이미 취소됨, 기간 경과 등)"),
                    @ApiResponse(responseCode = "401", description = "인증 실패"),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (타인의 예약)"),
                    @ApiResponse(responseCode = "404", description = "예약을 찾을 수 없음")
            }
    )
    @DeleteMapping("/cancel/{reservationId}")
    public ResponseEntity<BaseResponse<String>> deleteReservation(
            @Parameter(description = "예약 ID", required = true, example = "1")
            @PathVariable Long reservationId,

            @Parameter(description = "사용자 ID (JWT에서 추출)", required = true)
            @RequestHeader("X-User-Id") Long userId) {

        log.info("예약 취소 - reservationId: {}, userId: {}", reservationId, userId);
        reservationWebPort.cancel(reservationId, userId);
        return ResponseEntity.ok(BaseResponse.success("예약이 취소되었습니다."));
    }

    // ========== 내부 통신 API (대시보드용) ==========

    /**
     * 특정 기업의 전체 예약 수 조회
     */
    @Operation(
            summary = "특정 기업의 전체 예약 수 조회",
            description = "관리자 대시보드에서 사용할 특정 기업의 전체 예약 수를 조회합니다. (내부 통신용)"
    )
    @GetMapping("/company-counts/{companyId}")
    public Integer getAllReservationCountsByCompany(
            @Parameter(description = "기업 ID", required = true, example = "1")
            @PathVariable Long companyId) {

        return reservationWebPort.getAllReservationCountsByCompany(companyId);
    }

    /**
     * 리소스 그룹별 예약 트렌드 조회
     */
    @Operation(
            summary = "리소스 그룹별 예약 트렌드 조회",
            description = "최근 한 달간 리소스 그룹별 일별 예약 수를 조회합니다. (내부 통신용)"
    )
    @PostMapping("/trends")
    public List<ReservationTrendDto> getReservationCountsByGroupResources(
            @RequestBody ReservationTrendCommand dto) {

        return reservationWebPort.getReservationCountsByGroupResources(dto);
    }

    /**
     * 리소스 그룹별 누적 예약 수 조회
     */
    @Operation(
            summary = "리소스 그룹별 누적 예약 수 조회",
            description = "여러 리소스 그룹의 누적 예약 수를 일괄 조회합니다. (내부 통신용)"
    )
    @PostMapping("/group-counts")
    public List<ServiceGroupDashBoardDto.GroupReservationCountResponse> getCumReservationCountsByGroupResources(
            @RequestBody List<Long> groupIds) {

        return reservationWebPort.getCumReservationCountsByGroupResources(groupIds);
    }

    /**
     * 리소스 그룹의 누적 예약 수 조회
     */
    @Operation(
            summary = "리소스 그룹의 누적 예약 수 조회",
            description = "특정 리소스 그룹의 누적 예약 수를 조회합니다. (내부 통신용)"
    )
    @GetMapping("/cum-reservation/{resourceGroupId}")
    public Integer getCumReservationCount(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        return reservationWebPort.getCumReservationCount(resourceGroupId);
    }

    /**
     * 리소스 그룹의 누적 취소 수 조회
     */
    @Operation(
            summary = "리소스 그룹의 누적 취소 수 조회",
            description = "특정 리소스 그룹의 누적 취소 수를 조회합니다. (내부 통신용)"
    )
    @GetMapping("/cum-cancel/{resourceGroupId}")
    public Integer getCumCancelCount(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        return reservationWebPort.getCumCancelCount(resourceGroupId);
    }

    /**
     * 서비스별 성과 조회
     */
    @Operation(
            summary = "서비스별 성과 조회",
            description = "리소스 그룹에 속한 각 리소스별 예약 수를 조회합니다. (내부 통신용)"
    )
    @GetMapping("/resource-performance/{resourceGroupId}")
    public List<ServiceGroupDashBoardDto.ServicePerformanceCount> getServicePerformanceCount(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        return reservationWebPort.getServicePerformanceCount(resourceGroupId);
    }

    /**
     * 이용자 수 조회
     */
    @Operation(
            summary = "이용자 수 조회",
            description = "리소스 그룹의 총 이용자 수와 중복 제거된 실 이용자 수를 조회합니다. (내부 통신용)"
    )
    @GetMapping("/visitor/{resourceGroupId}/{companyId}")
    public ServiceGroupDashBoardDto.VisitorCount getVisitorCount(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId,

            @Parameter(description = "기업 ID", required = true, example = "1")
            @PathVariable Long companyId) {

        return reservationWebPort.getVisitorCount(resourceGroupId, companyId, UserRole.USER);
    }

    /**
     * 성별 예약 통계 조회
     */
    @Operation(
            summary = "성별 예약 통계 조회",
            description = "리소스 그룹의 성별 예약 통계를 조회합니다. (내부 통신용)"
    )
    @GetMapping("/gender/{resourceGroupId}")
    public List<ServiceGroupDashBoardDto.GenderReservationCount> getGenderReservationCount(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        return reservationWebPort.getGenderReservationCount(resourceGroupId);
    }

    /**
     * 나이대별 예약 통계 조회
     */
    @Operation(
            summary = "나이대별 예약 통계 조회",
            description = "리소스 그룹의 나이대별 예약 통계를 조회합니다. (내부 통신용)"
    )
    @GetMapping("/age/{resourceGroupId}")
    public List<ServiceGroupDashBoardDto.AgeReservationCount> getAgeReservationCount(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        return reservationWebPort.getAgeReservationCount(resourceGroupId);
    }

    /**
     * 시간대별 예약 현황 조회
     */
    @Operation(
            summary = "시간대별 예약 현황 조회",
            description = "리소스 그룹의 시간대별 예약 현황을 조회합니다. (하루 기준, 내부 통신용)"
    )
    @GetMapping("/time-slot/{resourceGroupId}")
    public List<ServiceGroupDashBoardDto.TimeSlotReservationCount> getTimeSlotReservationCount(
            @Parameter(description = "리소스 그룹 ID", required = true, example = "1")
            @PathVariable Long resourceGroupId) {

        return reservationWebPort.getTimeSlotReservationCount(resourceGroupId);
    }
}