package co.edu.udea.agrocore.backend.infrastructure.adapter.out.messaging;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.out.TelemetryEventPublisherPort;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging.dto.TelemetryEvent;
import co.edu.udea.agrocore.backend.infrastructure.config.AgrocoreKafkaProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Adapter de salida que publica eventos de telemetria al broker Kafka.
 *
 * Mapea el modelo de dominio TelemetryReading al evento de transporte
 * TelemetryEvent y lo serializa a JSON via el ObjectMapper de la app
 * (que incluye JavaTimeModule para LocalDateTime).
 *
 * La key del mensaje es idCropBatch (String), garantizando orden por lote
 * dentro de una particion.
 */
@Component
public class TelemetryKafkaProducer implements TelemetryEventPublisherPort {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public TelemetryKafkaProducer(KafkaTemplate<String, String> kafkaTemplate,
                                  ObjectMapper objectMapper,
                                  AgrocoreKafkaProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = properties.topics().telemetry();
    }

    @Override
    public void publish(TelemetryReading reading) {
        TelemetryEvent event = new TelemetryEvent(
                reading.getIdCropBatch(),
                reading.getRecordedAt(),
                reading.getTemperature(),
                reading.getHumidity(),
                reading.getCo2()
        );
        String payload = serialize(event);
        kafkaTemplate.send(topic, reading.getIdCropBatch().toString(), payload);
    }

    private String serialize(TelemetryEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar TelemetryEvent", e);
        }
    }
}
