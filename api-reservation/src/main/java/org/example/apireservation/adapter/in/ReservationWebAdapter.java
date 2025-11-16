package org.example.apireservation.adapter.in;

import lombok.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.apireservation.domain.model.Gender;
import org.example.apireservation.domain.model.dto.ReservationDetailDto;
import org.example.apireservation.domain.model.dto.ReservationListDto;
import org.example.apireservation.domain.model.dto.ReservationTrendDto;
import org.example.apireservation.domain.model.dto.ServiceGroupDashBoardDto;
import org.example.apireservation.usecase.port.in.*;
import org.example.common.base.BaseResponse;
import org.example.common.user.AuthDto;
import org.example.common.user.UserRole;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** ReservationController */
@Tag(name = "예약 처리 기능", description = "예약 요청, 조회, 취소 등 예약 처리에 대한 전반적인 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationWebAdapter {

    private final ReservationWebPort reservationWebPort;


    // ========================== 예약 요청 ==========================
    @Operation(summary = "예약 요청", description = "일반 사용자가 특정 기업의 사이트에서 예약/신청에 대한 요청을 합니다.")
    @PostMapping("/{resourceId}")
    public ResponseEntity<BaseResponse<ReservationDetailDto.Response>> createReservation(
            @Valid @RequestBody ReservationCommand dto,
            @PathVariable Long resourceId,
            @RequestAttribute("authUser") AuthDto authUser) {

        ReservationDetailDto.Response response = reservationWebPort.reserve(dto, resourceId, authUser);
        return ResponseEntity.ok(BaseResponse.success(response));
    }


    // ========================== 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자 "리소스 그룹"의 목록 ==========================
    @Operation(summary = "플랫폼 관리자 및 기업 관리자 예약 목록 조회", description = "플랫폼 관리자 및 기업 관리자가 리소스 그룹에 예약된 모든 예약/신청된 목록 조회를 합니다.")
    @GetMapping("/list/all/{resourceGroupId}")
    public ResponseEntity<BaseResponse<ReservationListDto.ResponseList>> getAdminReservations(@PathVariable Long resourceGroupId) {
        return ResponseEntity.ok(BaseResponse.success(reservationWebPort.getAdminReservations(resourceGroupId)));
    }


    // ========================== 특정 서비스의 예약 목록 조회 - 플랫폼 관리자 및 기업 관리자 ==========================
    @Operation(summary = "특정 서비스의 예약 목록 조회", description = "플랫폼 관리자 및 기업 관리자가 특정 리소스에 대한 예약/신청된 목록 조회를 합니다.")
    @GetMapping("/list/{resourceId}")
    public BaseResponse<ReservationListDto.ResponseList> getResourceReservations(
            @PathVariable Long resourceId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        ReservationListDto.ResponseList result = reservationWebPort.getResourceReservations(resourceId, startDate, endDate);
        return BaseResponse.success(result);
    }

    // ========================== 예약 목록 조회- 일반 사용자 ==========================
    @Operation(summary = "일반 사용자 예약 목록 조회", description = "일반 사용자가 예약/신청에 대한 목록 조회를 합니다.")
    @GetMapping("/list")
    public ResponseEntity<BaseResponse<ReservationListDto.UserResponseList>> getUserReservations(@RequestAttribute("authUser") AuthDto authUser) {
        return ResponseEntity.ok(BaseResponse.success(reservationWebPort.getUserReservations(authUser.getId())));
    }


    // ========================== 예약 상세 조회 ==========================
    @Operation(summary = "예약 상세 조회", description = "모든 사용자가 특정 기업 서비스의 예약/신청에 대한 상세 조회를 합니다.")
    @GetMapping("/detail/{reservationId}")
    public ResponseEntity<BaseResponse<ReservationDetailDto.Response>> getReservationDetail(@PathVariable Long reservationId) {
        return ResponseEntity.ok(BaseResponse.success(reservationWebPort.getReservationDetail(reservationId)));
    }


    // ========================== 예약 취소 ==========================
    @Operation(summary = "예약 취소", description = "모든 사용자가 특정 기업의 서비스 예약/신청에 대한 예약 취소 요청을 합니다.")
    @DeleteMapping("/cancel/{reservationId}")
    public ResponseEntity<BaseResponse<String>> deleteReservation(
            @PathVariable Long reservationId,
            @RequestAttribute("authUser") AuthDto authUser) {

        reservationWebPort.cancel(reservationId, authUser.getId());
        return ResponseEntity.ok(BaseResponse.success("예약이 취소 되었습니다."));
    }


    /**
     * 외부 통신을 위한 api
     * 관리자 전체 대시보드 */
    // ========================== 특정 기업의 전체 예약 수 조회 ==========================
    @Operation(summary = "특정 기업의 전체 예약수 조회")
    @PostMapping("/company-counts/{companyId}")
    public Integer getAllReservationCountsByCompany(@PathVariable Long companyId) {
        return reservationWebPort.getAllReservationCountsByCompany(companyId);
    }


    // ========================== 특정 기간 동안의 리소스 그룹별 예약 수 조회 ==========================
    @Operation(summary = "리소스 그룹별 예약수 조회")
    @PostMapping("/group-counts")
    public List<ReservationTrendDto> getReservationCountsByGroupResources(@RequestBody ReservationTrendCommand dto) {
        return reservationWebPort.getReservationCountsByGroupResources(dto);
    }


    /**
     * 외부 통신을 위한 api
     * 리소스 그룹별 대시보드 */
    // ========================== 리소스 그룹의 누적 예약수 ==========================
    @Operation(summary = "누적 예약수")
    @GetMapping("/cum-reservation/{resourceGroupId}")
    public Integer getCumReservationCount(@PathVariable Long resourceGroupId) {
        return reservationWebPort.getCumReservationCount(resourceGroupId);
    }


    // ========================== 리소스 그룹의 누적 취소 예약 수==========================
    @Operation(summary = "누적 취소 수")
    @GetMapping("/cum-cancel/{resourceGroupId}")
    public Integer getCumCancelCount(@PathVariable Long resourceGroupId) {
        return reservationWebPort.getCumCancelCount(resourceGroupId);
    }


    // ========================== 리소스 그룹에 속하는 리소스 수 (한달 기준) ==========================
    @Operation(summary = "서비스별 성과")
    @GetMapping("/resource-performance/{resourceGroupId}")
    public List<ServiceGroupDashBoardDto.ServicePerformanceCount> getServicePerformanceCount(@PathVariable Long resourceGroupId) {
        return reservationWebPort.getServicePerformanceCount(resourceGroupId);
    }


    // ========================== 리소스 그룹에 속하는 사용자 (중복제거) ==========================
    @Operation(summary = "이용자 수")
    @GetMapping("/visitor/{resourceGroupId}/{companyId}")
    public ServiceGroupDashBoardDto.VisitorCount getVisitorCount(
            @PathVariable Long resourceGroupId,
            @PathVariable Long companyId) {
        return reservationWebPort.getVisitorCount(resourceGroupId, companyId, UserRole.USER);
    }


    // ========================== 성별 ==========================
    @Operation(summary = "사용자 특성별 이용 - 성별")
    @GetMapping("/gender/{resourceGroupId}")
    public List<ServiceGroupDashBoardDto.GenderReservationCount> getGenderReservationCount(@PathVariable Long resourceGroupId) {
        return reservationWebPort.getGenderReservationCount(resourceGroupId);
    }


    // ========================== 나이대 ==========================
    @Operation(summary = "사용자 특성별 이용 - 나이")
    @GetMapping("/age/{resourceGroupId}")
    public List<ServiceGroupDashBoardDto.AgeReservationCount> getAgeReservationCount(@PathVariable Long resourceGroupId) {
        return reservationWebPort.getAgeReservationCount(resourceGroupId);
    }


    // ========================== 리소스 그룹에 속하는 시간대 별 예약 수 (하루 기준) ==========================
    @Operation(summary = "시간대별 예약 현황")
    @GetMapping("/time-slot/{resourceGroupId}")
    public List<ServiceGroupDashBoardDto.TimeSlotReservationCount> getTimeSlotReservationCount(@PathVariable Long resourceGroupId) {
        return reservationWebPort.getTimeSlotReservationCount(resourceGroupId);
    }
}
