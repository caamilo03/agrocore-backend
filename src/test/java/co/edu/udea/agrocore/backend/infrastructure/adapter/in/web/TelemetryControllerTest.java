package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.in.QueryTelemetryUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TelemetryController.class)
class TelemetryControllerTest {

    private static final String BASE = "/api/v1/telemetry/batches";
    private static final UUID BATCH_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QueryTelemetryUseCase queryTelemetryUseCase;

    @Test
    void latest_returnsReadingWhenPresent() throws Exception {
        when(queryTelemetryUseCase.getLatest(BATCH_ID)).thenReturn(Optional.of(sample(99L)));

        mockMvc.perform(get(BASE + "/{id}/latest", BATCH_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.idCropBatch").value(BATCH_ID.toString()))
                .andExpect(jsonPath("$.temperature").value(22.50));
    }

    @Test
    void latest_returns404WhenNoReadings() throws Exception {
        when(queryTelemetryUseCase.getLatest(BATCH_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE + "/{id}/latest", BATCH_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void recent_usesDefaultLimitWhenAbsent() throws Exception {
        when(queryTelemetryUseCase.getRecent(eq(BATCH_ID), eq(100))).thenReturn(List.of(sample(1L)));

        mockMvc.perform(get(BASE + "/{id}/recent", BATCH_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(queryTelemetryUseCase).getRecent(BATCH_ID, 100);
    }

    @Test
    void recent_clampsLimitAboveMax() throws Exception {
        when(queryTelemetryUseCase.getRecent(eq(BATCH_ID), eq(1000))).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/{id}/recent", BATCH_ID).param("limit", "9999"))
                .andExpect(status().isOk());

        verify(queryTelemetryUseCase).getRecent(BATCH_ID, 1000);
    }

    @Test
    void recent_clampsLimitBelowOne() throws Exception {
        when(queryTelemetryUseCase.getRecent(eq(BATCH_ID), eq(1))).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/{id}/recent", BATCH_ID).param("limit", "-5"))
                .andExpect(status().isOk());

        verify(queryTelemetryUseCase).getRecent(BATCH_ID, 1);
    }

    @Test
    void range_returnsReadings() throws Exception {
        LocalDateTime from = LocalDateTime.parse("2026-05-01T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2026-05-02T00:00:00");
        when(queryTelemetryUseCase.getInRange(BATCH_ID, from, to, 5000))
                .thenReturn(List.of(sample(10L), sample(11L)));

        mockMvc.perform(get(BASE + "/{id}/range", BATCH_ID)
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void range_returns400WhenFromAfterTo() throws Exception {
        LocalDateTime from = LocalDateTime.parse("2026-05-02T00:00:00");
        LocalDateTime to = LocalDateTime.parse("2026-05-01T00:00:00");

        mockMvc.perform(get(BASE + "/{id}/range", BATCH_ID)
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(queryTelemetryUseCase);
    }

    private TelemetryReading sample(Long id) {
        return TelemetryReading.builder()
                .id(id)
                .idCropBatch(BATCH_ID)
                .recordedAt(LocalDateTime.parse("2026-05-03T12:00:00"))
                .temperature(new BigDecimal("22.50"))
                .humidity(new BigDecimal("65.00"))
                .co2(new BigDecimal("420.00"))
                .build();
    }
}
