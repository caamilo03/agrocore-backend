package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.*;
import co.edu.udea.agrocore.backend.domain.port.out.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CropBatchTraceabilityServiceTest {

    private static final UUID BATCH_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID SPECIES_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID SUBSTRATE_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID SP_SUPPLIER_ID = UUID.fromString("99999999-1111-1111-1111-111111111111");
    private static final UUID SU_SUPPLIER_ID = UUID.fromString("99999999-2222-2222-2222-222222222222");
    private static final Instant FIXED_NOW = Instant.parse("2026-05-13T15:00:00Z");

    private CropBatchRepositoryPort cropBatchPort;
    private SpeciesRepositoryPort speciesPort;
    private SubstrateRepositoryPort substratePort;
    private SupplierRepositoryPort supplierPort;
    private TelemetryRepositoryPort telemetryPort;
    private CropBatchTraceabilityService service;

    @BeforeEach
    void setUp() {
        cropBatchPort = mock(CropBatchRepositoryPort.class);
        speciesPort = mock(SpeciesRepositoryPort.class);
        substratePort = mock(SubstrateRepositoryPort.class);
        supplierPort = mock(SupplierRepositoryPort.class);
        telemetryPort = mock(TelemetryRepositoryPort.class);
        Clock fixedClock = Clock.fixed(FIXED_NOW, ZoneId.of("UTC"));
        service = new CropBatchTraceabilityService(
                cropBatchPort, speciesPort, substratePort, supplierPort, telemetryPort, fixedClock);
    }

    @Test
    void get_composesFullView() {
        CropBatch batch = harvestedBatch();
        Species species = completeSpecies();
        Substrate substrate = new Substrate(SUBSTRATE_ID, "Fibra de coco", "desc");
        Supplier spSupplier = new Supplier(SP_SUPPLIER_ID, "Semillas SAS", "sp@x.com");
        Supplier suSupplier = new Supplier(SU_SUPPLIER_ID, "Sustratos LTDA", "su@x.com");
        TelemetryStats stats = new TelemetryStats(86400L,
                bd("22.40"), bd("14.10"), bd("31.20"), bd("87.50"),
                bd("68.30"), bd("42.00"), bd("91.00"), bd("76.20"),
                bd("480.50"), bd("320.00"), bd("720.00"), bd("91.00"));

        when(cropBatchPort.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(speciesPort.findById(SPECIES_ID)).thenReturn(Optional.of(species));
        when(substratePort.findById(SUBSTRATE_ID)).thenReturn(Optional.of(substrate));
        when(supplierPort.findById(SP_SUPPLIER_ID)).thenReturn(Optional.of(spSupplier));
        when(supplierPort.findById(SU_SUPPLIER_ID)).thenReturn(Optional.of(suSupplier));
        when(telemetryPort.computeStats(eq(BATCH_ID), any(), any(), any())).thenReturn(stats);

        TraceabilityView view = service.get(BATCH_ID);

        assertThat(view.batch()).isSameAs(batch);
        assertThat(view.species()).isSameAs(species);
        assertThat(view.substrate()).isSameAs(substrate);
        assertThat(view.speciesSupplier()).isSameAs(spSupplier);
        assertThat(view.substrateSupplier()).isSameAs(suSupplier);
        assertThat(view.telemetryStats()).isSameAs(stats);
    }

    @Test
    void get_usesBatchStartAndEndDatesForStatsRange() {
        CropBatch batch = harvestedBatch();
        when(cropBatchPort.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(speciesPort.findById(SPECIES_ID)).thenReturn(Optional.of(completeSpecies()));
        when(telemetryPort.computeStats(any(), any(), any(), any())).thenReturn(TelemetryStats.empty());

        service.get(BATCH_ID);

        Instant expectedFrom = batch.getStartDate().toInstant(ZoneOffset.UTC);
        Instant expectedTo = batch.getEndDate().toInstant(ZoneOffset.UTC);
        verify(telemetryPort).computeStats(eq(BATCH_ID), eq(expectedFrom), eq(expectedTo), any(OptimalRanges.class));
    }

    @Test
    void get_usesNowAsEndForActiveBatches() {
        // Lote ACTIVO sin endDate -> usar Instant.now(clock) como cota superior.
        CropBatch batch = harvestedBatch();
        batch.setStatus(CropBatchStatus.ACTIVO);
        batch.setEndDate(null);
        when(cropBatchPort.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(speciesPort.findById(SPECIES_ID)).thenReturn(Optional.of(completeSpecies()));
        when(telemetryPort.computeStats(any(), any(), any(), any())).thenReturn(TelemetryStats.empty());

        service.get(BATCH_ID);

        verify(telemetryPort).computeStats(eq(BATCH_ID), any(), eq(FIXED_NOW), any());
    }

    @Test
    void get_returnsNullStatsWhenNoTelemetryReadings() {
        CropBatch batch = harvestedBatch();
        when(cropBatchPort.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(speciesPort.findById(SPECIES_ID)).thenReturn(Optional.of(completeSpecies()));
        when(telemetryPort.computeStats(any(), any(), any(), any())).thenReturn(TelemetryStats.empty());

        TraceabilityView view = service.get(BATCH_ID);

        assertThat(view.telemetryStats()).isNull();
    }

    @Test
    void get_returnsNullStatsWhenSpeciesHasIncompleteRanges() {
        CropBatch batch = harvestedBatch();
        Species incomplete = completeSpecies();
        incomplete.setMaxCo2(null); // rangos incompletos
        when(cropBatchPort.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(speciesPort.findById(SPECIES_ID)).thenReturn(Optional.of(incomplete));

        TraceabilityView view = service.get(BATCH_ID);

        assertThat(view.telemetryStats()).isNull();
        verify(telemetryPort, never()).computeStats(any(), any(), any(), any());
    }

    @Test
    void get_returnsNullForMissingReferences() {
        CropBatch batch = harvestedBatch();
        when(cropBatchPort.findById(BATCH_ID)).thenReturn(Optional.of(batch));
        when(speciesPort.findById(any())).thenReturn(Optional.empty());
        when(substratePort.findById(any())).thenReturn(Optional.empty());
        when(supplierPort.findById(any())).thenReturn(Optional.empty());

        TraceabilityView view = service.get(BATCH_ID);

        assertThat(view.batch()).isSameAs(batch);
        assertThat(view.species()).isNull();
        assertThat(view.substrate()).isNull();
        assertThat(view.speciesSupplier()).isNull();
        assertThat(view.substrateSupplier()).isNull();
        assertThat(view.telemetryStats()).isNull(); // no species -> no stats
    }

    @Test
    void get_throwsNotFoundWhenBatchMissing() {
        when(cropBatchPort.findById(BATCH_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(BATCH_ID))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void get_handlesNullForeignKeyIdsGracefully() {
        // Lote con FKs nulas no debe llamar a los repos de referencia.
        CropBatch batch = CropBatch.builder()
                .id(BATCH_ID)
                .startDate(LocalDateTime.of(2026, 4, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 5, 1, 0, 0))
                .status(CropBatchStatus.COSECHADO)
                .build();
        when(cropBatchPort.findById(BATCH_ID)).thenReturn(Optional.of(batch));

        TraceabilityView view = service.get(BATCH_ID);

        assertThat(view.species()).isNull();
        verify(speciesPort, never()).findById(any());
        verify(substratePort, never()).findById(any());
        verify(supplierPort, never()).findById(any());
    }

    // -- helpers --

    private CropBatch harvestedBatch() {
        return CropBatch.builder()
                .id(BATCH_ID)
                .idSpecies(SPECIES_ID)
                .idSubstrate(SUBSTRATE_ID)
                .idSpeciesSupplier(SP_SUPPLIER_ID)
                .idSubstrateSupplier(SU_SUPPLIER_ID)
                .idUser(UUID.randomUUID())
                .startDate(LocalDateTime.of(2026, 4, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 5, 1, 0, 0))
                .status(CropBatchStatus.COSECHADO)
                .yieldKg(bd("12.50"))
                .build();
    }

    private Species completeSpecies() {
        return Species.builder()
                .idSpecies(SPECIES_ID)
                .name("Tomate")
                .minTemperature(bd("18.00")).maxTemperature(bd("28.00"))
                .minHumidity(bd("60.00")).maxHumidity(bd("80.00"))
                .minCo2(bd("350.00")).maxCo2(bd("600.00"))
                .build();
    }

    private static BigDecimal bd(String s) { return new BigDecimal(s); }
}
