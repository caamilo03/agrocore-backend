package co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.in.SaveTelemetryReadingUseCase;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging.dto.TelemetryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consume eventos del topic agrocore.telemetry.readings.v1, los traduce
 * al modelo de dominio y delega la persistencia al caso de uso.
 *
 * Payloads viajan como String JSON (ver KafkaJacksonConfig). El ObjectMapper
 * inyectado es el autoconfigurado por Spring Boot, que ya incluye JavaTimeModule.
 *
 * El manejo de errores (reintentos + DLQ) lo aporta el DefaultErrorHandler
 * declarado en KafkaConsumerConfig: si el parse JSON o el save lanzan
 * excepcion, el handler reintenta y eventualmente publica en el topic DLQ.
 */
@Component
public class TelemetryConsumer {

    private static final Logger log = LoggerFactory.getLogger(TelemetryConsumer.class);

    private final SaveTelemetryReadingUseCase saveTelemetryReadingUseCase;
    private final ObjectMapper objectMapper;

    public TelemetryConsumer(SaveTelemetryReadingUseCase saveTelemetryReadingUseCase,
                             ObjectMapper objectMapper) {
        this.saveTelemetryReadingUseCase = saveTelemetryReadingUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${agrocore.kafka.topics.telemetry}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void onMessage(String payload) {
        TelemetryEvent event = parse(payload);

        log.debug("Recibido evento de telemetria para batch={} en {}",
                event.idCropBatch(), event.recordedAt());

        TelemetryReading reading = TelemetryReading.builder()
                .idCropBatch(event.idCropBatch())
                .recordedAt(event.recordedAt())
                .temperature(event.temperature())
                .humidity(event.humidity())
                .co2(event.co2())
                .build();

        saveTelemetryReadingUseCase.save(reading);
    }

    private TelemetryEvent parse(String payload) {
        try {
            return objectMapper.readValue(payload, TelemetryEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Payload de telemetria invalido", e);
        }
    }
}
