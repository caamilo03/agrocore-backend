package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.application.exception.ForbiddenException;
import co.edu.udea.agrocore.backend.application.exception.InvalidBatchStateException;
import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.model.CropBatchStatus;
import co.edu.udea.agrocore.backend.domain.model.Role;
import co.edu.udea.agrocore.backend.domain.port.in.*;
import co.edu.udea.agrocore.backend.domain.port.out.CropBatchRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.security.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class CropBatchService implements CreateCropBatchUseCase, GetAllCropBatchUseCase,
        UpdateCropBatchUseCase, DeleteCropBatchUseCase, HarvestCropBatchUseCase {

    private final CropBatchRepositoryPort repositoryPort;
    private final Clock clock;
    private final AuthenticatedUser authenticatedUser;

    public CropBatchService(CropBatchRepositoryPort repositoryPort,
                            Clock clock,
                            AuthenticatedUser authenticatedUser) {
        this.repositoryPort = repositoryPort;
        this.clock = clock;
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public CropBatch create(CropBatch cropBatch) {
        cropBatch.setId(UUID.randomUUID());
        // OPERADOR solo puede crear lotes propios — sobreescribir idUser para prevenir spoofing.
        if (isOperador()) {
            cropBatch.setIdUser(currentUserId());
        }
        return repositoryPort.save(cropBatch);
    }

    @Override
    public List<CropBatch> getAll() {
        return getAll(null);
    }

    @Override
    public List<CropBatch> getAll(CropBatchStatus statusFilter) {
        // OPERADOR solo ve sus propios lotes; ADMIN y OBSERVADOR ven todos.
        if (isOperador()) {
            return repositoryPort.findByUserId(currentUserId(), statusFilter);
        }
        if (statusFilter != null) {
            return repositoryPort.findByStatus(statusFilter);
        }
        return repositoryPort.findAll();
    }

    @Override
    public CropBatch update(UUID id, CropBatch cropBatch) {
        CropBatch existing = loadAndAssertAccess(id);
        cropBatch.setId(id);
        // Preservar el idUser original — no permitir cambio de dueño vía PUT.
        cropBatch.setIdUser(existing.getIdUser());
        return repositoryPort.save(cropBatch);
    }

    @Override
    public void delete(UUID id) {
        loadAndAssertAccess(id);
        repositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public CropBatch harvest(UUID id, BigDecimal yieldKg, Instant endDate) {
        if (yieldKg == null || yieldKg.signum() <= 0) {
            throw new IllegalArgumentException("yieldKg debe ser un numero positivo");
        }
        CropBatch batch = loadAndAssertAccess(id);
        if (batch.getStatus() != CropBatchStatus.ACTIVO) {
            throw new InvalidBatchStateException(
                    "El lote no se puede cosechar: estado actual es " + batch.getStatus());
        }
        Instant effectiveEnd = endDate != null ? endDate : Instant.now(clock);
        batch.setStatus(CropBatchStatus.COSECHADO);
        batch.setYieldKg(yieldKg);
        batch.setEndDate(LocalDateTime.ofInstant(effectiveEnd, ZoneOffset.UTC));
        return repositoryPort.save(batch);
    }

    // ----- helpers -----

    /**
     * Carga el lote y verifica que el usuario actual tenga acceso.
     * ADMIN siempre puede; OPERADOR solo si es dueño.
     */
    private CropBatch loadAndAssertAccess(UUID id) {
        CropBatch batch = repositoryPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lote no encontrado"));
        assertAccess(batch);
        return batch;
    }

    private void assertAccess(CropBatch batch) {
        if (isOperador() && !currentUserId().equals(batch.getIdUser())) {
            throw new ForbiddenException("No tienes permiso para acceder a este lote");
        }
    }

    private boolean isOperador() {
        return authenticatedUser.getRole()
                .map(r -> r == Role.OPERADOR)
                .orElse(false);
    }

    private UUID currentUserId() {
        return authenticatedUser.getUserId()
                .orElseThrow(() -> new NoSuchElementException("Usuario no autenticado"));
    }
}
