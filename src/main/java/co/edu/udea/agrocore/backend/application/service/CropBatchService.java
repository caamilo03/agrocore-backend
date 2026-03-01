package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.port.in.CreateCropBatchUseCase;
import co.edu.udea.agrocore.backend.domain.port.out.CropBatchRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CropBatchService implements CreateCropBatchUseCase {

    // Inyectar el puerto de salida
    private final CropBatchRepositoryPort repositoryPort;

    @Override
    public CropBatch create(CropBatch cropBatch) {
        //Reglas de negocio básicas antes de guardar
        if (cropBatch.getStartDate() == null) {
            cropBatch.setStartDate(LocalDateTime.now());
        }

        if (cropBatch.getStatus() == null || cropBatch.getStatus().isEmpty()) {
            cropBatch.setStatus("ACTIVE"); // Estado por defecto
        }

        // Puerto de salida guarda
        return repositoryPort.save(cropBatch);
    }
}