package co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.in.SaveTelemetryReadingUseCase;
import co.edu.udea.agrocore.backend.infrastructure.config.AgrocoreRedisProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class TelemetryRedisStreamConsumerTest {

    private static final String STREAM = "agrocore.telemetry.readings.v1";
    private static final String GROUP = "agrocore-telemetry-group";

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private SaveTelemetryReadingUseCase useCase;
    private StringRedisTemplate redisTemplate;
    @SuppressWarnings("rawtypes")
    private StreamOperations streamOps;
    private TelemetryRedisStreamConsumer consumer;

    @BeforeEach
    void setUp() {
        useCase = mock(SaveTelemetryReadingUseCase.class);
        redisTemplate = mock(StringRedisTemplate.class);
        streamOps = mock(StreamOperations.class);
        when(redisTemplate.opsForStream()).thenReturn(streamOps);

        AgrocoreRedisProperties props = new AgrocoreRedisProperties(
                new AgrocoreRedisProperties.Streams(STREAM, GROUP, "test-consumer", 10000L, 1000L)
        );
        consumer = new TelemetryRedisStreamConsumer(useCase, mapper, redisTemplate, props);
    }

    @Test
    void onMessage_parsesJsonAndDelegatesToUseCase() {
        UUID batchId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        String payload = """
                {
                  "idCropBatch": "11111111-1111-1111-1111-111111111111",
                  "recordedAt": "2026-05-03T12:00:00Z",
                  "temperature": 23.40,
                  "humidity": 70.10,
                  "co2": 415.50
                }
                """;
        RecordId recordId = RecordId.of("1700000000000-0");
        MapRecord<String, String, String> record = MapRecord.create(STREAM, Map.of("data", payload))
                .withId(recordId);

        consumer.onMessage(record);

        ArgumentCaptor<TelemetryReading> captor = ArgumentCaptor.forClass(TelemetryReading.class);
        verify(useCase, times(1)).save(captor.capture());

        TelemetryReading saved = captor.getValue();
        assertThat(saved.getId()).isNull();
        assertThat(saved.getIdCropBatch()).isEqualTo(batchId);
        assertThat(saved.getRecordedAt()).isEqualTo(Instant.parse("2026-05-03T12:00:00Z"));
        assertThat(saved.getTemperature()).isEqualByComparingTo("23.40");
        assertThat(saved.getHumidity()).isEqualByComparingTo("70.10");
        assertThat(saved.getCo2()).isEqualByComparingTo("415.50");

        verify(streamOps, times(1)).acknowledge(STREAM, GROUP, recordId);
    }

    @Test
    void onMessage_loggsAndAcksOnInvalidJson() {
        RecordId recordId = RecordId.of("1700000000001-0");
        MapRecord<String, String, String> record = MapRecord.create(
                STREAM, Map.of("data", "{ this is not valid json")
        ).withId(recordId);

        consumer.onMessage(record);

        verifyNoInteractions(useCase);
        verify(streamOps, times(1)).acknowledge(STREAM, GROUP, recordId);
    }

    @Test
    void onMessage_acksWhenPayloadFieldMissing() {
        RecordId recordId = RecordId.of("1700000000002-0");
        MapRecord<String, String, String> record = MapRecord.create(
                STREAM, Map.of("other", "value")
        ).withId(recordId);

        consumer.onMessage(record);

        verifyNoInteractions(useCase);
        verify(streamOps, times(1)).acknowledge(STREAM, GROUP, recordId);
    }
}
