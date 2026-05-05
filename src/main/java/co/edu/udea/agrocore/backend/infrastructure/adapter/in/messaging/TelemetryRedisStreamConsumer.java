package co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.in.SaveTelemetryReadingUseCase;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging.dto.TelemetryEvent;
import co.edu.udea.agrocore.backend.infrastructure.config.AgrocoreRedisProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

/**
 * Consume eventos del stream Redis configurado, los traduce al modelo de
 * dominio y delega la persistencia al caso de uso. Tras procesar (con exito
 * o error registrado) se hace XACK para retirar el record de la PEL.
 *
 * Errores de parse o de save se loggean y se ackea igualmente: la simulacion
 * sigue produciendo lecturas y queremos evitar que un payload corrupto
 * bloquee el consumo. La BD es la fuente de verdad del historico, no el
 * stream.
 */
@Component
public class TelemetryRedisStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private static final Logger log = LoggerFactory.getLogger(TelemetryRedisStreamConsumer.class);
    private static final String PAYLOAD_FIELD = "data";

    private final SaveTelemetryReadingUseCase saveTelemetryReadingUseCase;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final String stream;
    private final String consumerGroup;

    public TelemetryRedisStreamConsumer(SaveTelemetryReadingUseCase saveTelemetryReadingUseCase,
                                        ObjectMapper objectMapper,
                                        StringRedisTemplate redisTemplate,
                                        AgrocoreRedisProperties properties) {
        this.saveTelemetryReadingUseCase = saveTelemetryReadingUseCase;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.stream = properties.streams().telemetry();
        this.consumerGroup = properties.streams().consumerGroup();
    }

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        try {
            String payload = record.getValue().get(PAYLOAD_FIELD);
            if (payload == null) {
                throw new IllegalArgumentException("Record sin campo '" + PAYLOAD_FIELD + "'");
            }
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
        } catch (Exception e) {
            log.error("Error procesando record {} del stream {}: {}",
                    record.getId(), stream, e.getMessage(), e);
        } finally {
            redisTemplate.opsForStream().acknowledge(stream, consumerGroup, record.getId());
        }
    }

    private TelemetryEvent parse(String payload) {
        try {
            return objectMapper.readValue(payload, TelemetryEvent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Payload de telemetria invalido", e);
        }
    }
}
