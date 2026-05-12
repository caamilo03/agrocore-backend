package co.edu.udea.agrocore.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agrocore.simulator")
public record AgrocoreSimulatorProperties(
        boolean enabled,
        long intervalMs,
        int variancePercent
) {
}
