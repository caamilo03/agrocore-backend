package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.out.TelemetryRepositoryPort;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TelemetryServiceTest {

    @Test
    void save_delegatesToRepositoryPortAndReturnsPersistedReading() {
        TelemetryRepositoryPort port = mock(TelemetryRepositoryPort.class);
        TelemetryService service = new TelemetryService(port);

        TelemetryReading input = TelemetryReading.builder()
                .idCropBatch(UUID.randomUUID())
                .recordedAt(LocalDateTime.now())
                .temperature(new BigDecimal("22.50"))
                .humidity(new BigDecimal("65.00"))
                .co2(new BigDecimal("420.00"))
                .build();

        TelemetryReading persisted = TelemetryReading.builder()
                .id(1L)
                .idCropBatch(input.getIdCropBatch())
                .recordedAt(input.getRecordedAt())
                .temperature(input.getTemperature())
                .humidity(input.getHumidity())
                .co2(input.getCo2())
                .build();

        when(port.save(input)).thenReturn(persisted);

        TelemetryReading result = service.save(input);

        assertThat(result).isSameAs(persisted);
        verify(port, times(1)).save(input);
        verifyNoMoreInteractions(port);
    }
}
