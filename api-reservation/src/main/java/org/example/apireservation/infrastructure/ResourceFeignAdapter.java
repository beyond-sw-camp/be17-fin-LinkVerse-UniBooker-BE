package org.example.apireservation.infrastructure;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.apireservation.domain.model.CustomFieldValue;
import org.example.apireservation.domain.model.Resource;
import org.example.apireservation.domain.model.ResourceGroup;
import org.example.apireservation.usecase.port.in.CustomFieldValueCommand;
import org.example.common.base.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@FeignClient(name="api-resource")
public interface ResourceFeignAdapter {
    // ========================== 상세 조회 ==========================
    @CircuitBreaker(name = "GET_RESOURCE_GROUP_API")
    @GetMapping("/api/resource/all/{resourceId}")
    BaseResponse<Resource> findById(@PathVariable("resourceId") Long resourceId);


    // ========================== 상세 조회 (활성화 & 미삭제 상태만) ==========================
    @CircuitBreaker(name = "GET_RESOURCE_GROUP_API")
    @GetMapping("/api/resource/{resourceId}")
    BaseResponse<Resource> findResourceById(@PathVariable("resourceId") Long resourceId);


    // ========================== 상세 조회 (활성화 & 미삭제 상태 & 비관적 락) ==========================
    @CircuitBreaker(name = "GET_RESOURCE_GROUP_API")
    @GetMapping("/api/resource/pessimistic/{resourceId}")
    BaseResponse<Resource> findResourceByIdForUpdate(@PathVariable("resourceId") Long resourceId);


    // ========================== 리소스 그룹 상세 조회 ==========================
    @CircuitBreaker(name = "GET_RESOURCE_GROUP_API")
    @GetMapping("/api/resource-group/{resourceGroupId}")
    Optional<ResourceGroup> findByIdAndDeletedAtIsNull(@PathVariable("resourceGroupId") Long resourceGroupId);


    // ========================== 사용자 입력 커스텀 필드 값 조회 ==========================
    @CircuitBreaker(name = "GET_USER_FIELD_VALUES_API")
    @GetMapping("/api/custom-field/value/reservation/{reservationId}")
    BaseResponse<List<CustomFieldValue>> getUserFieldValuesByReservation(@PathVariable("reservationId") Long reservationId);


    // ========================== 사용자 커스텀 필드 값 등록 ==========================
    @CircuitBreaker(name = "REGISTER_USER_FIELD_VALUES_API")
    @PostMapping("/api/custom-field/values/{targetId}")
    List<CustomFieldValue> register(
            @PathVariable("targetId") Long reservationId,
            @RequestBody List<CustomFieldValueCommand> dto);
}
