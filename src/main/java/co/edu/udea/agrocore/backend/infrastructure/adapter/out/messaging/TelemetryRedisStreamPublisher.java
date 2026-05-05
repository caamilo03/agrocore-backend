package co.edu.udea.agrocore.backend.infrastructure.adapter.out.messaging;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.out.TelemetryEventPublisherPort;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging.dto.TelemetryEvent;
import co.edu.udea.agrocore.backend.infrastructure.config.AgrocoreRedisProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adapter de salida que publica eventos de telemetria a un stream Redis.
 *
 * El payload viaja como un campo "data" con JSON serializado (mismo formato
 * que tenia con Kafka), de modo que cambiar el transporte no obligo a
 * modificar el contrato del evento (TelemetryEvent).
 *
 * Tras cada XADD aplicamos un trim aproximado (~) para mantener el stream
 * acotado en RAM; el historico real vive en Postgres.
 */
@Component
public class TelemetryRedisStreamPublisher implements TelemetryEventPublisherPort {

    private static final String PAYLOAD_FIELD = "data";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final String stream;
    private final long maxLen;

    public TelemetryRedisStreamPublisher(StringRedisTemplate redisTemplate,
                                         ObjectMapper objectMapper,
                                         AgrocoreRedisProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.stream = properties.streams().telemetry();
        this.maxLen = properties.streams().maxLen();
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

        redisTemplate.opsForStream().add(
                StreamRecords.string(Map.of(PAYLOAD_FIELD, payload)).withStreamKey(stream)
        );
        redisTemplate.opsForStream().trim(stream, maxLen, true);
    }

    private String serialize(TelemetryEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo serializar TelemetryEvent", e);
        }
    }
}
