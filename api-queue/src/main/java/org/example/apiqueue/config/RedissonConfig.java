package org.example.apiqueue.config;

import lombok.RequiredArgsConstructor;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SentinelProperties.class})
@RequiredArgsConstructor
public class RedissonConfig {

    private final SentinelProperties sentinelProps;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        // List<String> → "redis://host:port" 배열 변환
        String[] sentinelAddresses = sentinelProps.getNodes().stream()
                .map(node -> "redis://" + node)
                .toArray(String[]::new);

        config.useSentinelServers()
                .setMasterName(sentinelProps.getMaster())
                .addSentinelAddress(sentinelAddresses)
                .setCheckSentinelsList(true)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        return Redisson.create(config);
    }
}
