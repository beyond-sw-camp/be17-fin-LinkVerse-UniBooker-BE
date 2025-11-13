package org.example.apireservation.infrastructure;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.apireservation.domain.model.CustomFieldValue;
import org.example.apireservation.domain.model.Resource;
import org.example.common.base.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@FeignClient(name="resourceClient", url = "http://localhost:8081")
public interface ResourceFeignAdapter {
    // ========================== 상세 조회 ==========================
    @CircuitBreaker(name = "")
    @GetMapping("/api/resource/detail/{resourceId}")
    BaseResponse<Resource> findById(Long resourceId);


    // ========================== 상세 조회 (활성화 & 미삭제 상태만) ==========================
    @CircuitBreaker(name = "")
    @GetMapping("/api/resource/{resourceId}/super")
    BaseResponse<Resource> findResourceById(Long resourceId);


    // ========================== 상세 조회 (활성화 & 미삭제 상태 & 비관적 락) ==========================
    @CircuitBreaker(name = "")
    @GetMapping("/api/resource/pessimistic/{resourceId}")
    BaseResponse<Resource> findResourceByIdForUpdate(@PathVariable("resourceId") Long resourceId);
}
