package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.application.exception.InvalidBatchStateException;
import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.model.CropBatchStatus;
import co.edu.udea.agrocore.backend.domain.port.out.CropBatchRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
import static org.mockito.Mockito.*;

class CropBatchServiceTest {

    private static final UUID BATCH_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final Instant FIXED_NOW = Instant.parse("2026-05-13T15:00:00Z");

    private CropBatchRepositoryPort port;
    private CropBatchService service;

    @BeforeEach
    void setUp() {
        port = mock(CropBatchRepositoryPort.class);
        Clock fixedClock = Clock.fixed(FIXED_NOW, ZoneId.of("UTC"));
        service = new CropBatchService(port, fixedClock);
    }

    // ----- harvest -----

    @Test
    void harvest_marksBatchAsCosechadoWithYieldAndEndDate() {
        CropBatch active = activeBatch();
        when(port.findById(BATCH_ID)).thenReturn(Optional.of(active));
        when(port.save(any(CropBatch.class))).thenAnswer(inv -> inv.getArgument(0));

        Instant endDate = Instant.parse("2026-05-13T10:30:00Z");
        CropBatch result = service.harvest(BATCH_ID, new BigDecimal("12.50"), endDate);

        assertThat(result.getStatus()).isEqualTo(CropBatchStatus.COSECHADO);
        assertThat(result.getYieldKg()).isEqualByComparingTo("12.50");
        assertThat(result.getEndDate()).isEqualTo(LocalDateTime.ofInstant(endDate, ZoneOffset.UTC));
    }

    @Test
    void harvest_usesNowWhenEndDateIsNull() {
        CropBatch active = activeBatch();
        when(port.findById(BATCH_ID)).thenReturn(Optional.of(active));
        ArgumentCaptor<CropBatch> captor = ArgumentCaptor.forClass(CropBatch.class);
        when(port.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.harvest(BATCH_ID, new BigDecimal("5.00"), null);

        // FIXED_NOW = 2026-05-13T15:00:00Z = 2026-05-13T15:00:00 UTC LocalDateTime
        assertThat(captor.getValue().getEndDate())
                .isEqualTo(LocalDateTime.of(2026, 5, 13, 15, 0, 0));
    }

    @Test
    void harvest_throwsConflictWhenAlreadyHarvested() {
        CropBatch harvested = batchWithStatus(CropBatchStatus.COSECHADO);
        when(port.findById(BATCH_ID)).thenReturn(Optional.of(harvested));

        assertThatThrownBy(() -> service.harvest(BATCH_ID, new BigDecimal("1.0"), null))
                .isInstanceOf(InvalidBatchStateException.class)
                .hasMessageContaining("COSECHADO");

        verify(port, never()).save(any());
    }

    @Test
    void harvest_throwsConflictWhenBatchIsPerdido() {
        CropBatch perdido = batchWithStatus(CropBatchStatus.PERDIDO);
        when(port.findById(BATCH_ID)).thenReturn(Optional.of(perdido));

        assertThatThrownBy(() -> service.harvest(BATCH_ID, new BigDecimal("1.0"), null))
                .isInstanceOf(InvalidBatchStateException.class)
                .hasMessageContaining("PERDIDO");
    }

    @Test
    void harvest_throwsNotFoundWhenBatchMissing() {
        when(port.findById(BATCH_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.harvest(BATCH_ID, new BigDecimal("1.0"), null))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void harvest_throwsBadRequestForNullYield() {
        assertThatThrownBy(() -> service.harvest(BATCH_ID, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positivo");
        verifyNoInteractions(port);
    }

    @Test
    void harvest_throwsBadRequestForZeroYield() {
        assertThatThrownBy(() -> service.harvest(BATCH_ID, BigDecimal.ZERO, null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(port);
    }

    @Test
    void harvest_throwsBadRequestForNegativeYield() {
        assertThatThrownBy(() -> service.harvest(BATCH_ID, new BigDecimal("-1.0"), null))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(port);
    }

    // ----- update (smoke test for the existing behavior — ensures the
    // service still works after adding the Clock dependency) -----

    @Test
    void update_throwsNotFoundWhenBatchMissing() {
        when(port.existsById(BATCH_ID)).thenReturn(false);
        assertThatThrownBy(() -> service.update(BATCH_ID, activeBatch()))
                .isInstanceOf(NoSuchElementException.class);
    }

    // ----- helpers -----

    private CropBatch activeBatch() {
        return batchWithStatus(CropBatchStatus.ACTIVO);
    }

    private CropBatch batchWithStatus(CropBatchStatus status) {
        return CropBatch.builder()
                .id(BATCH_ID)
                .idSpecies(UUID.randomUUID())
                .idSubstrate(UUID.randomUUID())
                .idSpeciesSupplier(UUID.randomUUID())
                .idSubstrateSupplier(UUID.randomUUID())
                .idUser(UUID.randomUUID())
                .startDate(LocalDateTime.of(2026, 4, 1, 0, 0))
                .status(status)
                .build();
    }
}
