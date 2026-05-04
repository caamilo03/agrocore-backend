package co.edu.udea.agrocore.backend.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Define el ObjectMapper de la aplicacion con soporte para tipos Java 8
 * (LocalDateTime, etc.) y serializacion de fechas como ISO-8601 strings.
 *
 * Necesario porque Spring Boot 4 modular no autoconfigura ObjectMapper
 * solo con spring-boot-starter-webmvc.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}