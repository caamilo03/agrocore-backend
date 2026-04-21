package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.port.in.*;
import co.edu.udea.agrocore.backend.domain.port.out.CropBatchRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CropBatchService implements CreateCropBatchUseCase, GetAllCropBatchUseCase, UpdateCropBatchUseCase, DeleteCropBatchUseCase {

    private final CropBatchRepositoryPort repositoryPort;

    public CropBatchService(CropBatchRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
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
    public CropBatch update(UUID id, CropBatch cropBatch) {
        if (!repositoryPort.existsById(id)) {
            throw new RuntimeException("Lote no encontrado");
        }
        cropBatch.setId(id);
        return repositoryPort.save(cropBatch);
    }

    @Override
    public void delete(UUID id) {
        repositoryPort.deleteById(id);
    }
}