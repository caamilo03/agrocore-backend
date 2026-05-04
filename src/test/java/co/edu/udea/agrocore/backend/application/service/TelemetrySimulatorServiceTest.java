package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.model.Species;
import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.out.CropBatchRepositoryPort;
import co.edu.udea.agrocore.backend.domain.port.out.SpeciesRepositoryPort;
import co.edu.udea.agrocore.backend.domain.port.out.TelemetryEventPublisherPort;
import co.edu.udea.agrocore.backend.infrastructure.config.AgrocoreSimulatorProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TelemetrySimulatorServiceTest {

    private static final UUID SPECIES_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID BATCH_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private CropBatchRepositoryPort batchPort;
    private SpeciesRepositoryPort speciesPort;
    private TelemetryEventPublisherPort publisher;
    private TelemetrySimulatorService service;

    @BeforeEach
    void setUp() {
        batchPort = mock(CropBatchRepositoryPort.class);
        speciesPort = mock(SpeciesRepositoryPort.class);
        publisher = mock(TelemetryEventPublisherPort.class);

        AgrocoreSimulatorProperties props = new AgrocoreSimulatorProperties(true, 5000L, 5);
        Clock fixedClock = Clock.fixed(Instant.parse("2026-05-03T12:00:00Z"), ZoneId.of("UTC"));
        Random deterministicRandom = new Random(42L); // semilla fija para reproducibilidad

        service = new TelemetrySimulatorService(
                batchPort, speciesPort, publisher, props, fixedClock, deterministicRandom
        );
    }

    @Test
    void tick_skipsPublishWhenNoActiveBatches() {
        when(batchPort.findByStatus("ACTIVE")).thenReturn(List.of());

        service.tick();

        verifyNoInteractions(publisher);
    }

    @Test
    void tick_publishesOneReadingPerActiveBatchWithCompleteSpecies() {
        when(batchPort.findByStatus("ACTIVE")).thenReturn(List.of(activeBatch()));
        when(speciesPort.findById(SPECIES_ID)).thenReturn(Optional.of(completeSpecies()));

        service.tick();

        verify(publisher, times(1)).publish(any(TelemetryReading.class));
    }

    @Test
    void tick_skipsBatchWithoutSpecies() {
        CropBatch orphan = CropBatch.builder()
                .id(BATCH_ID)
                .idSpecies(null)
                .status("ACTIVE")
                .build();
        when(batchPort.findByStatus("ACTIVE")).thenReturn(List.of(orphan));

        service.tick();

        verifyNoInteractions(publisher);
        verifyNoInteractions(speciesPort);
    }

    @Test
    void tick_skipsBatchWhenSpeciesHasIncompleteRanges() {
        Species incomplete = Species.builder()
                .idSpecies(SPECIES_ID)
                .name("Incompleta")
                .minTemperature(new BigDecimal("18.00"))
                .maxTemperature(new BigDecimal("28.00"))
                // sin humedad ni co2
                .build();
        when(batchPort.findByStatus("ACTIVE")).thenReturn(List.of(activeBatch()));
        when(speciesPort.findById(SPECIES_ID)).thenReturn(Optional.of(incomplete));

        service.tick();

        verifyNoInteractions(publisher);
    }

    @Test
    void generatedReadingStaysWithinSpeciesRanges() {
        Species species = completeSpecies();

        // 100 ticks deberian mantenerse dentro del rango (clamp activo).
        when(batchPort.findByStatus("ACTIVE")).thenReturn(List.of(activeBatch()));
        when(speciesPort.findById(SPECIES_ID)).thenReturn(Optional.of(species));

        ArgumentCaptor<TelemetryReading> captor = ArgumentCaptor.forClass(TelemetryReading.class);

        for (int i = 0; i < 100; i++) {
            service.tick();
        }
        verify(publisher, times(100)).publish(captor.capture());

        for (TelemetryReading r : captor.getAllValues()) {
            assertThat(r.getTemperature())
                    .isBetween(species.getMinTemperature(), species.getMaxTemperature());
            assertThat(r.getHumidity())
                    .isBetween(species.getMinHumidity(), species.getMaxHumidity());
            assertThat(r.getCo2())
                    .isBetween(species.getMinCo2(), species.getMaxCo2());
            assertThat(r.getIdCropBatch()).isEqualTo(BATCH_ID);
            assertThat(r.getRecordedAt()).isNotNull();
        }
    }

    @Test
    void firstReadingStartsNearMidpointOfRanges() {
        Species species = completeSpecies();
        when(batchPort.findByStatus("ACTIVE")).thenReturn(List.of(activeBatch()));
        when(speciesPort.findById(SPECIES_ID)).thenReturn(Optional.of(species));

        ArgumentCaptor<TelemetryReading> captor = ArgumentCaptor.forClass(TelemetryReading.class);
        service.tick();
        verify(publisher).publish(captor.capture());

        TelemetryReading first = captor.getValue();
        // El primer valor parte del midpoint y se le suma una sola muestra de ruido,
        // asi que debe estar relativamente cerca del centro del rango.
        BigDecimal tempMid = species.getMinTemperature()
                .add(species.getMaxTemperature())
                .divide(BigDecimal.valueOf(2), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal range = species.getMaxTemperature().subtract(species.getMinTemperature());

        // tolerancia: 3 sigma con variancePercent=5 => 15% del rango
        BigDecimal tolerance = range.multiply(new BigDecimal("0.15"));
        assertThat(first.getTemperature())
                .isBetween(tempMid.subtract(tolerance), tempMid.add(tolerance));
    }

    @Test
    void speciesIsCachedAcrossTicks() {
        when(batchPort.findByStatus("ACTIVE")).thenReturn(List.of(activeBatch()));
        when(speciesPort.findById(SPECIES_ID)).thenReturn(Optional.of(completeSpecies()));

        service.tick();
        service.tick();
        service.tick();

        verify(speciesPort, times(1)).findById(SPECIES_ID);
        verify(publisher, times(3)).publish(any(TelemetryReading.class));
    }

    private CropBatch activeBatch() {
        return CropBatch.builder()
                .id(BATCH_ID)
                .idSpecies(SPECIES_ID)
                .status("ACTIVE")
                .build();
    }

    private Species completeSpecies() {
        return Species.builder()
                .idSpecies(SPECIES_ID)
                .name("Tomate cherry")
                .minTemperature(new BigDecimal("18.00"))
                .maxTemperature(new BigDecimal("28.00"))
                .minHumidity(new BigDecimal("60.00"))
                .maxHumidity(new BigDecimal("80.00"))
                .minCo2(new BigDecimal("350.00"))
                .maxCo2(new BigDecimal("500.00"))
                .build();
    }
}
