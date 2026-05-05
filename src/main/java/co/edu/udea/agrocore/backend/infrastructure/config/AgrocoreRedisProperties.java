package co.edu.udea.agrocore.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agrocore.redis")
public record AgrocoreRedisProperties(Streams streams) {
    public record Streams(
            String telemetry,
            String consumerGroup,
            String consumerName,
            long maxLen,
            long pollTimeoutMs
    ) {}
}
