package co.edu.udea.agrocore.backend.infrastructure.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Manejo de errores para consumidores Kafka:
 *   - 3 reintentos con backoff exponencial (500ms inicial, multiplicador x2).
 *   - Tras agotar reintentos, el record se publica en el topic DLQ
 *     (mismo nombre con sufijo configurado en agrocore.kafka.topics.telemetry-dlq).
 */
@Configuration
public class KafkaConsumerConfig {

    private final AgrocoreKafkaProperties properties;

    public KafkaConsumerConfig(AgrocoreKafkaProperties properties) {
        this.properties = properties;
    }

    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(properties.topics().telemetryDlq(), record.partition())
        );

        ExponentialBackOff backOff = new ExponentialBackOff(500L, 2.0);
        backOff.setMaxAttempts(3);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
