package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.port.in.CreateCropBatchUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.GetCropBatchUseCase;
import co.edu.udea.agrocore.backend.domain.port.out.CropBatchRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CropBatchService implements CreateCropBatchUseCase, GetCropBatchUseCase {

    private final CropBatchRepositoryPort repositoryPort;

    @Override
    public CropBatch create(CropBatch cropBatch) {
        if (cropBatch.getStartDate() == null) {
            cropBatch.setStartDate(LocalDateTime.now());
        }
        if (cropBatch.getStatus() == null || cropBatch.getStatus().isEmpty()) {
            cropBatch.setStatus("ACTIVE");
        }
        return repositoryPort.save(cropBatch);
    }

    // <-- Añadimos este bloque nuevo
    @Override
    public List<CropBatch> getAll() {
        return repositoryPort.findAll();
    }
}
