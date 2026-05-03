package co.edu.udea.agrocore.backend.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declaracion de topics. Spring crea/actualiza estos topics al arrancar
 * via KafkaAdmin (autoconfigurado por spring-boot-starter-kafka).
 */
@Configuration
public class KafkaTopicConfig {

    private final AgrocoreKafkaProperties properties;

    public KafkaTopicConfig(AgrocoreKafkaProperties properties) {
        this.properties = properties;
    }

    @Bean
    public NewTopic telemetryTopic() {
        return TopicBuilder.name(properties.topics().telemetry())
                .partitions(properties.partitions())
                .replicas(properties.replicationFactor())
                .build();
    }

    @Bean
    public NewTopic telemetryDlqTopic() {
        return TopicBuilder.name(properties.topics().telemetryDlq())
                .partitions(properties.partitions())
                .replicas(properties.replicationFactor())
                .build();
    }
}
