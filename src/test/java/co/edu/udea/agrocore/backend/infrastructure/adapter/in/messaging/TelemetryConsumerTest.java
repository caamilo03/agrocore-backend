package co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.in.SaveTelemetryReadingUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TelemetryConsumerTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void onMessage_parsesJsonAndDelegatesToUseCase() {
        SaveTelemetryReadingUseCase useCase = mock(SaveTelemetryReadingUseCase.class);
        TelemetryConsumer consumer = new TelemetryConsumer(useCase, mapper);

        UUID batchId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        String payload = """
                {
                  "idCropBatch": "11111111-1111-1111-1111-111111111111",
                  "recordedAt": "2026-05-03T12:00:00",
                  "temperature": 23.40,
                  "humidity": 70.10,
                  "co2": 415.50
                }
                """;

        consumer.onMessage(payload);

        ArgumentCaptor<TelemetryReading> captor = ArgumentCaptor.forClass(TelemetryReading.class);
        verify(useCase, times(1)).save(captor.capture());

        TelemetryReading saved = captor.getValue();
        assertThat(saved.getId()).isNull();
        assertThat(saved.getIdCropBatch()).isEqualTo(batchId);
        assertThat(saved.getRecordedAt()).hasToString("2026-05-03T12:00");
        assertThat(saved.getTemperature()).isEqualByComparingTo("23.40");
        assertThat(saved.getHumidity()).isEqualByComparingTo("70.10");
        assertThat(saved.getCo2()).isEqualByComparingTo("415.50");
    }

    @Test
    void onMessage_throwsOnInvalidJson() {
        SaveTelemetryReadingUseCase useCase = mock(SaveTelemetryReadingUseCase.class);
        TelemetryConsumer consumer = new TelemetryConsumer(useCase, mapper);

        assertThatThrownBy(() -> consumer.onMessage("{ this is not valid json"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payload de telemetria invalido");

        verifyNoInteractions(useCase);
    }
}
