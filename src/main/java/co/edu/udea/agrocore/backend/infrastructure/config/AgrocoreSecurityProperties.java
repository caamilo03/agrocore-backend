package co.edu.udea.agrocore.backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "agrocore.security")
public record AgrocoreSecurityProperties(
        Jwt jwt,
        List<String> adminEmails,
        Google google
) {
    public record Jwt(String secret, long expiryMs) {}
    public record Google(String clientId) {}
}
