package co.edu.udea.agrocore.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agrocore.kafka")
public record AgrocoreKafkaProperties(
        Topics topics,
        int partitions,
        short replicationFactor
) {
    public record Topics(String telemetry, String telemetryDlq) {}
}
