package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.application.exception.InvalidBatchStateException;
import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.model.CropBatchStatus;
import co.edu.udea.agrocore.backend.domain.port.in.*;
import co.edu.udea.agrocore.backend.domain.port.out.CropBatchRepositoryPort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class CropBatchService implements CreateCropBatchUseCase, GetAllCropBatchUseCase, UpdateCropBatchUseCase,
        DeleteCropBatchUseCase, HarvestCropBatchUseCase {

    private final CropBatchRepositoryPort repositoryPort;
    private final Clock clock;

    public CropBatchService(CropBatchRepositoryPort repositoryPort, Clock clock) {
        this.repositoryPort = repositoryPort;
        this.clock = clock;
    }

    @Override
    public CropBatch create(CropBatch cropBatch) {
        cropBatch.setId(UUID.randomUUID());
        return repositoryPort.save(cropBatch);
    }

    @Override
    public List<CropBatch> getAll() {
        return repositoryPort.findAll();
    }

    @Override
    public List<CropBatch> getAll(CropBatchStatus statusFilter) {
        if (statusFilter == null) {
            return repositoryPort.findAll();
        }
        return repositoryPort.findByStatus(statusFilter);
    }

    @Override
    public CropBatch update(UUID id, CropBatch cropBatch) {
        if (!repositoryPort.existsById(id)) {
            throw new NoSuchElementException("Lote no encontrado");
        }
        cropBatch.setId(id);
        return repositoryPort.save(cropBatch);
    }

    @Override
    public void delete(UUID id) {
        repositoryPort.deleteById(id);
    }

    @Override
    public CropBatch harvest(UUID id, BigDecimal yieldKg, Instant endDate) {
        if (yieldKg == null || yieldKg.signum() <= 0) {
            throw new IllegalArgumentException("yieldKg debe ser un numero positivo");
        }
        CropBatch batch = repositoryPort.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Lote no encontrado"));
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
}
