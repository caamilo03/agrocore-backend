package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.application.exception.InvalidBatchStateException;
import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.model.CropBatchStatus;
import co.edu.udea.agrocore.backend.domain.port.in.CreateCropBatchUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.DeleteCropBatchUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.GetAllCropBatchUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.GetCropBatchTraceabilityUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.HarvestCropBatchUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.UpdateCropBatchUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CropBatchController.class)
@Import(GlobalExceptionHandler.class)
class CropBatchControllerTest {

    private static final String BASE = "/api/v1/batches";
    private static final UUID BATCH_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private CreateCropBatchUseCase createUseCase;
    @MockitoBean private GetAllCropBatchUseCase getAllUseCase;
    @MockitoBean private UpdateCropBatchUseCase updateUseCase;
    @MockitoBean private DeleteCropBatchUseCase deleteUseCase;
    @MockitoBean private HarvestCropBatchUseCase harvestUseCase;
    @MockitoBean private GetCropBatchTraceabilityUseCase traceabilityUseCase;

    // ----- POST /{id}/harvest -----

    @Test
    void harvest_returns200WhenSuccessful() throws Exception {
        CropBatch harvested = sampleHarvested();
        when(harvestUseCase.harvest(eq(BATCH_ID), any(BigDecimal.class), any())).thenReturn(harvested);

        mockMvc.perform(post(BASE + "/{id}/harvest", BATCH_ID)
                        .contentType("application/json")
                        .content("{\"yieldKg\": 12.50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COSECHADO"))
                .andExpect(jsonPath("$.yieldKg").value(12.50));
    }

    @Test
    void harvest_returns409WhenBatchAlreadyHarvested() throws Exception {
        when(harvestUseCase.harvest(eq(BATCH_ID), any(BigDecimal.class), any()))
                .thenThrow(new InvalidBatchStateException("El lote no se puede cosechar: estado actual es COSECHADO"));

        mockMvc.perform(post(BASE + "/{id}/harvest", BATCH_ID)
                        .contentType("application/json")
                        .content("{\"yieldKg\": 5.0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("COSECHADO")));
    }

    @Test
    void harvest_returns404WhenBatchMissing() throws Exception {
        when(harvestUseCase.harvest(eq(BATCH_ID), any(BigDecimal.class), any()))
                .thenThrow(new NoSuchElementException("Lote no encontrado"));

        mockMvc.perform(post(BASE + "/{id}/harvest", BATCH_ID)
                        .contentType("application/json")
                        .content("{\"yieldKg\": 5.0}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void harvest_returns400WhenYieldInvalid() throws Exception {
        when(harvestUseCase.harvest(eq(BATCH_ID), any(BigDecimal.class), any()))
                .thenThrow(new IllegalArgumentException("yieldKg debe ser un numero positivo"));

        mockMvc.perform(post(BASE + "/{id}/harvest", BATCH_ID)
                        .contentType("application/json")
                        .content("{\"yieldKg\": -1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void harvest_returns400WhenEndDateUnparseable() throws Exception {
        mockMvc.perform(post(BASE + "/{id}/harvest", BATCH_ID)
                        .contentType("application/json")
                        .content("{\"yieldKg\": 5.0, \"endDate\": \"not-a-date\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(harvestUseCase);
    }

    @Test
    void harvest_acceptsEndDateWithZone() throws Exception {
        CropBatch harvested = sampleHarvested();
        when(harvestUseCase.harvest(eq(BATCH_ID), any(BigDecimal.class), any())).thenReturn(harvested);

        mockMvc.perform(post(BASE + "/{id}/harvest", BATCH_ID)
                        .contentType("application/json")
                        .content("{\"yieldKg\": 5.0, \"endDate\": \"2026-05-13T15:00:00Z\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void harvest_acceptsEndDateWithoutZone() throws Exception {
        CropBatch harvested = sampleHarvested();
        when(harvestUseCase.harvest(eq(BATCH_ID), any(BigDecimal.class), any())).thenReturn(harvested);

        mockMvc.perform(post(BASE + "/{id}/harvest", BATCH_ID)
                        .contentType("application/json")
                        .content("{\"yieldKg\": 5.0, \"endDate\": \"2026-05-13T15:00:00\"}"))
                .andExpect(status().isOk());
    }

    // ----- GET /batches?status= -----

    @org.junit.jupiter.api.Test
    void getAll_withoutStatusParam_returnsAll() throws Exception {
        when(getAllUseCase.getAll((CropBatchStatus) null))
                .thenReturn(java.util.List.of(sampleHarvested()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @org.junit.jupiter.api.Test
    void getAll_withCosechadoFilter_callsServiceWithEnum() throws Exception {
        when(getAllUseCase.getAll(CropBatchStatus.COSECHADO))
                .thenReturn(java.util.List.of(sampleHarvested()));

        mockMvc.perform(get(BASE).param("status", "COSECHADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("COSECHADO"));
    }

    @org.junit.jupiter.api.Test
    void getAll_withLowercaseStatus_isAccepted() throws Exception {
        when(getAllUseCase.getAll(CropBatchStatus.ACTIVO))
                .thenReturn(java.util.List.of());

        mockMvc.perform(get(BASE).param("status", "activo"))
                .andExpect(status().isOk());
    }

    @org.junit.jupiter.api.Test
    void getAll_withInvalidStatus_returns400() throws Exception {
        mockMvc.perform(get(BASE).param("status", "FOO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("FOO")));

        verifyNoInteractions(getAllUseCase);
    }

    // ----- GET /{id}/traceability -----

    @org.junit.jupiter.api.Test
    void traceability_returnsFullPayload() throws Exception {
        co.edu.udea.agrocore.backend.domain.model.Species species =
                co.edu.udea.agrocore.backend.domain.model.Species.builder()
                        .idSpecies(java.util.UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                        .name("Tomate")
                        .minTemperature(new BigDecimal("18.00"))
                        .maxTemperature(new BigDecimal("28.00"))
                        .minHumidity(new BigDecimal("60.00"))
                        .maxHumidity(new BigDecimal("80.00"))
                        .minCo2(new BigDecimal("350.00"))
                        .maxCo2(new BigDecimal("600.00"))
                        .build();
        co.edu.udea.agrocore.backend.domain.model.Substrate substrate =
                new co.edu.udea.agrocore.backend.domain.model.Substrate(
                        java.util.UUID.randomUUID(), "Fibra de coco", "desc");
        co.edu.udea.agrocore.backend.domain.model.Supplier supplier =
                new co.edu.udea.agrocore.backend.domain.model.Supplier(
                        java.util.UUID.randomUUID(), "Semillas SAS", "x@y.com");
        co.edu.udea.agrocore.backend.domain.model.TelemetryStats stats =
                new co.edu.udea.agrocore.backend.domain.model.TelemetryStats(
                        100L,
                        new BigDecimal("22.40"), new BigDecimal("14.10"), new BigDecimal("31.20"), new BigDecimal("87.50"),
                        new BigDecimal("68.30"), new BigDecimal("42.00"), new BigDecimal("91.00"), new BigDecimal("76.20"),
                        new BigDecimal("480.50"), new BigDecimal("320.00"), new BigDecimal("720.00"), new BigDecimal("91.00"));
        co.edu.udea.agrocore.backend.domain.model.TraceabilityView view =
                new co.edu.udea.agrocore.backend.domain.model.TraceabilityView(
                        sampleHarvested(), species, substrate, supplier, supplier, stats);
        when(traceabilityUseCase.get(BATCH_ID)).thenReturn(view);

        mockMvc.perform(get(BASE + "/{id}/traceability", BATCH_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batch.status").value("COSECHADO"))
                .andExpect(jsonPath("$.species.name").value("Tomate"))
                .andExpect(jsonPath("$.substrate.typeName").value("Fibra de coco"))
                .andExpect(jsonPath("$.speciesSupplier.nameSupplier").value("Semillas SAS"))
                .andExpect(jsonPath("$.substrateSupplier.nameSupplier").value("Semillas SAS"))
                .andExpect(jsonPath("$.telemetryStats.count").value(100))
                .andExpect(jsonPath("$.telemetryStats.temperatureInRangePct").value(87.50));
    }

    @org.junit.jupiter.api.Test
    void traceability_returnsPayloadWithNullSubobjects() throws Exception {
        co.edu.udea.agrocore.backend.domain.model.TraceabilityView view =
                new co.edu.udea.agrocore.backend.domain.model.TraceabilityView(
                        sampleHarvested(), null, null, null, null, null);
        when(traceabilityUseCase.get(BATCH_ID)).thenReturn(view);

        mockMvc.perform(get(BASE + "/{id}/traceability", BATCH_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batch.id").value(BATCH_ID.toString()))
                .andExpect(jsonPath("$.species").doesNotExist())
                .andExpect(jsonPath("$.telemetryStats").doesNotExist());
    }

    @org.junit.jupiter.api.Test
    void traceability_returns404WhenBatchMissing() throws Exception {
        when(traceabilityUseCase.get(BATCH_ID)).thenThrow(new NoSuchElementException("Lote no encontrado"));

        mockMvc.perform(get(BASE + "/{id}/traceability", BATCH_ID))
                .andExpect(status().isNotFound());
    }

    // ----- parseEndDate unit -----

    @Test
    void parseEndDate_returnsNullForBlankInput() {
        assertThat(CropBatchController.parseEndDate(null)).isNull();
        assertThat(CropBatchController.parseEndDate("")).isNull();
        assertThat(CropBatchController.parseEndDate("   ")).isNull();
    }

    @Test
    void parseEndDate_throwsForGarbageInput() {
        try {
            CropBatchController.parseEndDate("not-a-date");
            org.assertj.core.api.Assertions.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage()).contains("not-a-date");
        }
    }

    // ----- helper -----

    private CropBatch sampleHarvested() {
        return CropBatch.builder()
                .id(BATCH_ID)
                .status(CropBatchStatus.COSECHADO)
                .yieldKg(new BigDecimal("12.50"))
                .startDate(LocalDateTime.of(2026, 4, 1, 0, 0))
                .endDate(LocalDateTime.of(2026, 5, 13, 15, 0))
                .build();
    }
}
