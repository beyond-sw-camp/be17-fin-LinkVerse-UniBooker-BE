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
import org.example.common.model.UserRole;
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
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Company-Id") Long companyId) {

        ReservationDetailDto.Response response = reservationWebPort.reserve(dto, resourceId, userId, companyId);
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
    public ResponseEntity<BaseResponse<ReservationListDto.UserResponseList>> getUserReservations(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(BaseResponse.success(reservationWebPort.getUserReservations(userId)));
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
            @RequestHeader("X-User-Id") Long userId) {

        reservationWebPort.cancel(reservationId, userId);
        return ResponseEntity.ok(BaseResponse.success("예약이 취소 되었습니다."));
    }

    /**
     * 내부 통신을 위한 api
     * 관리자 전체 대시보드 */
    // ========================== 특정 기업의 전체 예약 수 조회 ==========================
    @Operation(summary = "특정 기업의 전체 예약수 조회")
    @GetMapping("/company-counts/{companyId}")  // ✅ POST → GET 변경
    public BaseResponse<Integer> getAllReservationCountsByCompany(@PathVariable Long companyId) {
        Integer count = reservationWebPort.getAllReservationCountsByCompany(companyId);
        return BaseResponse.success(count);  // ✅ BaseResponse로 래핑
    }

    // ========================== 특정 기간 동안의 리소스 그룹별 예약 수 조회 ==========================
    @Operation(summary = "리소스 그룹별 예약수 조회")
    @PostMapping("/group-counts")
    public BaseResponse<List<ReservationTrendDto>> getReservationCountsByGroupResources(
            @RequestBody ReservationTrendCommand dto) {
        List<ReservationTrendDto> result = reservationWebPort.getReservationCountsByGroupResources(dto);
        return BaseResponse.success(result);  // ✅ BaseResponse 래핑 추가
    }

    // ========================== 리소스 그룹별 예약 추이 조회 ==========================
    @Operation(summary = "리소스 그룹별 예약 추이 조회")
    @PostMapping("/trends")
    public BaseResponse<List<ReservationTrendDto>> getReservationTrends(
            @RequestBody ReservationTrendCommand command) {

        // 기존 메서드 재사용
        List<ReservationTrendDto> trends = reservationWebPort.getReservationCountsByGroupResources(command);
        return BaseResponse.success(trends);
    }

    /**
     * 내부 통신을 위한 api
     * 리소스 그룹별 대시보드 */
    // ========================== 리소스 그룹의 누적 예약수 ==========================
    @Operation(summary = "누적 예약수")
    @GetMapping("/cum-reservation/{resourceGroupId}")
    public BaseResponse<Integer> getCumReservationCount(@PathVariable Long resourceGroupId) {  // ✅ 반환 타입 변경
        Integer count = reservationWebPort.getCumReservationCount(resourceGroupId);
        return BaseResponse.success(count);  // ✅ BaseResponse 래핑 추가
    }

    // ========================== 리소스 그룹의 누적 취소 예약 수==========================
    @Operation(summary = "누적 취소 수")
    @GetMapping("/cum-cancel/{resourceGroupId}")
    public BaseResponse<Integer> getCumCancelCount(@PathVariable Long resourceGroupId) {  // ✅ 반환 타입 변경
        Integer count = reservationWebPort.getCumCancelCount(resourceGroupId);
        return BaseResponse.success(count);  // ✅ BaseResponse 래핑 추가
    }

    // ========================== 리소스 그룹에 속하는 리소스 수 (한달 기준) ==========================
    @Operation(summary = "서비스별 성과")
    @GetMapping("/resource-performance/{resourceGroupId}")
    public BaseResponse<List<ServiceGroupDashBoardDto.ServicePerformanceCount>> getServicePerformanceCount(
            @PathVariable Long resourceGroupId) {  // ✅ 반환 타입 변경
        List<ServiceGroupDashBoardDto.ServicePerformanceCount> result =
                reservationWebPort.getServicePerformanceCount(resourceGroupId);
        return BaseResponse.success(result);  // ✅ BaseResponse 래핑 추가
    }

    // ========================== 리소스 그룹에 속하는 사용자 (중복제거) ==========================
    @Operation(summary = "이용자 수")
    @GetMapping("/visitor/{resourceGroupId}/{companyId}")
    public BaseResponse<ServiceGroupDashBoardDto.VisitorCount> getVisitorCount(  // ✅ 반환 타입 변경
                                                                                 @PathVariable Long resourceGroupId,
                                                                                 @PathVariable Long companyId) {
        ServiceGroupDashBoardDto.VisitorCount result =
                reservationWebPort.getVisitorCount(resourceGroupId, companyId, UserRole.USER);
        return BaseResponse.success(result);  // ✅ BaseResponse 래핑 추가
    }

    // ========================== 성별 ==========================
    @Operation(summary = "사용자 특성별 이용 - 성별")
    @GetMapping("/gender/{resourceGroupId}")
    public BaseResponse<List<ServiceGroupDashBoardDto.GenderReservationCount>> getGenderReservationCount(
            @PathVariable Long resourceGroupId) {  // ✅ 반환 타입 변경
        List<ServiceGroupDashBoardDto.GenderReservationCount> result =
                reservationWebPort.getGenderReservationCount(resourceGroupId);
        return BaseResponse.success(result);  // ✅ BaseResponse 래핑 추가
    }

    // ========================== 나이대 ==========================
    @Operation(summary = "사용자 특성별 이용 - 나이")
    @GetMapping("/age/{resourceGroupId}")
    public BaseResponse<List<ServiceGroupDashBoardDto.AgeReservationCount>> getAgeReservationCount(
            @PathVariable Long resourceGroupId) {  // ✅ 반환 타입 변경
        List<ServiceGroupDashBoardDto.AgeReservationCount> result =
                reservationWebPort.getAgeReservationCount(resourceGroupId);
        return BaseResponse.success(result);  // ✅ BaseResponse 래핑 추가
    }

    // ========================== 리소스 그룹에 속하는 시간대 별 예약 수 (하루 기준) ==========================
    @Operation(summary = "시간대별 예약 현황")
    @GetMapping("/time-slot/{resourceGroupId}")
    public BaseResponse<List<ServiceGroupDashBoardDto.TimeSlotReservationCount>> getTimeSlotReservationCount(
            @PathVariable Long resourceGroupId) {  // ✅ 반환 타입 변경
        List<ServiceGroupDashBoardDto.TimeSlotReservationCount> result =
                reservationWebPort.getTimeSlotReservationCount(resourceGroupId);
        return BaseResponse.success(result);  // ✅ BaseResponse 래핑 추가
    }
}