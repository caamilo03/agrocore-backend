package co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.in.SaveTelemetryReadingUseCase;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.messaging.dto.TelemetryEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TelemetryConsumerTest {

    @Test
    void onMessage_mapsEventToReadingAndDelegatesToUseCase() {
        SaveTelemetryReadingUseCase useCase = mock(SaveTelemetryReadingUseCase.class);
        TelemetryConsumer consumer = new TelemetryConsumer(useCase);

        UUID batchId = UUID.randomUUID();
        LocalDateTime recordedAt = LocalDateTime.now();
        TelemetryEvent event = new TelemetryEvent(
                batchId,
                recordedAt,
                new BigDecimal("23.40"),
                new BigDecimal("70.10"),
                new BigDecimal("415.50")
        );

        consumer.onMessage(event);

        ArgumentCaptor<TelemetryReading> captor = ArgumentCaptor.forClass(TelemetryReading.class);
        verify(useCase, times(1)).save(captor.capture());

        TelemetryReading saved = captor.getValue();
        assertThat(saved.getId()).isNull(); // lo asigna BIGSERIAL en BD
        assertThat(saved.getIdCropBatch()).isEqualTo(batchId);
        assertThat(saved.getRecordedAt()).isEqualTo(recordedAt);
        assertThat(saved.getTemperature()).isEqualByComparingTo("23.40");
        assertThat(saved.getHumidity()).isEqualByComparingTo("70.10");
        assertThat(saved.getCo2()).isEqualByComparingTo("415.50");
    }
}
