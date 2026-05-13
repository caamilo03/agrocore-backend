package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.out.TelemetryRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TelemetryServiceTest {

    private TelemetryRepositoryPort port;
    private TelemetryService service;

    @BeforeEach
    void setUp() {
        port = mock(TelemetryRepositoryPort.class);
        service = new TelemetryService(port);
    }

    @Test
    void save_delegatesToRepositoryPortAndReturnsPersistedReading() {
        TelemetryReading input = sampleReading(null);
        TelemetryReading persisted = sampleReading(1L);
        when(port.save(input)).thenReturn(persisted);

        TelemetryReading result = service.save(input);

        assertThat(result).isSameAs(persisted);
        verify(port, times(1)).save(input);
        verifyNoMoreInteractions(port);
    }

    @Test
    void getLatest_delegatesToRepositoryPort() {
        UUID batchId = UUID.randomUUID();
        TelemetryReading reading = sampleReading(7L);
        when(port.findLatestByBatch(batchId)).thenReturn(Optional.of(reading));

        Optional<TelemetryReading> result = service.getLatest(batchId);

        assertThat(result).contains(reading);
        verify(port).findLatestByBatch(batchId);
    }

    @Test
    void getRecent_delegatesWithLimit() {
        UUID batchId = UUID.randomUUID();
        List<TelemetryReading> readings = List.of(sampleReading(1L), sampleReading(2L));
        when(port.findRecentByBatch(batchId, 50)).thenReturn(readings);

        List<TelemetryReading> result = service.getRecent(batchId, 50);

        assertThat(result).isEqualTo(readings);
        verify(port).findRecentByBatch(batchId, 50);
    }

    @Test
    void getInRange_delegatesWithBoundsAndLimit() {
        UUID batchId = UUID.randomUUID();
        Instant from = Instant.parse("2026-05-01T00:00:00Z");
        Instant to = Instant.parse("2026-05-02T00:00:00Z");
        List<TelemetryReading> readings = List.of(sampleReading(3L));
        when(port.findByBatchInRange(batchId, from, to, 5000)).thenReturn(readings);

        List<TelemetryReading> result = service.getInRange(batchId, from, to, 5000);

        assertThat(result).isEqualTo(readings);
        verify(port).findByBatchInRange(batchId, from, to, 5000);
    }

    @Test
    void getRepresentativeInRange_delegatesWithBoundsAndMaxBuckets() {
        UUID batchId = UUID.randomUUID();
        Instant from = Instant.parse("2026-05-01T00:00:00Z");
        Instant to = Instant.parse("2026-05-08T00:00:00Z");
        List<TelemetryReading> buckets = List.of(sampleReading(null), sampleReading(null));
        when(port.findRepresentativeInRange(batchId, from, to, 5000)).thenReturn(buckets);

        List<TelemetryReading> result = service.getRepresentativeInRange(batchId, from, to, 5000);

        assertThat(result).isEqualTo(buckets);
        verify(port).findRepresentativeInRange(batchId, from, to, 5000);
    }

    @Test
    void getStats_delegatesToRepositoryPort() {
        UUID batchId = UUID.randomUUID();
        Instant from = Instant.parse("2026-04-01T00:00:00Z");
        Instant to = Instant.parse("2026-05-01T00:00:00Z");
        co.edu.udea.agrocore.backend.domain.model.OptimalRanges ranges =
                new co.edu.udea.agrocore.backend.domain.model.OptimalRanges(
                        new BigDecimal("18.00"), new BigDecimal("28.00"),
                        new BigDecimal("60.00"), new BigDecimal("80.00"),
                        new BigDecimal("350.00"), new BigDecimal("600.00"));
        co.edu.udea.agrocore.backend.domain.model.TelemetryStats expected =
                co.edu.udea.agrocore.backend.domain.model.TelemetryStats.empty();
        when(port.computeStats(batchId, from, to, ranges)).thenReturn(expected);

        assertThat(service.getStats(batchId, from, to, ranges)).isSameAs(expected);
        verify(port).computeStats(batchId, from, to, ranges);
    }

    private TelemetryReading sampleReading(Long id) {
        return TelemetryReading.builder()
                .id(id)
                .idCropBatch(UUID.randomUUID())
                .recordedAt(Instant.now())
                .temperature(new BigDecimal("22.50"))
                .humidity(new BigDecimal("65.00"))
                .co2(new BigDecimal("420.00"))
                .build();
    }
}
