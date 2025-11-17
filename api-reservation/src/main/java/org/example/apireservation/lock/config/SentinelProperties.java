package org.example.apireservation.lock.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.data.redis.sentinel")
public class SentinelProperties {

    private String master;        // spring.data.redis.sentinel.master
    private List<String> nodes;   // spring.data.redis.sentinel.nodes
}
