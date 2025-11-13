package org.example.apistatistics.infrastructure;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="resourceClient", url = "http://localhost:808")
public class ResourceFeignAdapter {

    // 리소스
}
