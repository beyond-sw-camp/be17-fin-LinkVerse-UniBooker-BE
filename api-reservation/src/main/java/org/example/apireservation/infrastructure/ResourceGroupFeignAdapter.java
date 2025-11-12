package org.example.apireservation.infrastructure;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.apireservation.domain.model.ResourceGroup;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name="resourceGroupClient", url = "http://localhost:8081")
public interface ResourceGroupFeignAdapter {

    // ========================== 리소스 그룹 상세 조회 ==========================
    @CircuitBreaker(name = "GET_RESOURCE_GROUP_API")
    @GetMapping("/api/resource-group/{resourceGroupId}")
    Optional<ResourceGroup> findByIdAndDeletedAtIsNull(@PathVariable("resourceGroupId") Long resourceGroupId);
}
