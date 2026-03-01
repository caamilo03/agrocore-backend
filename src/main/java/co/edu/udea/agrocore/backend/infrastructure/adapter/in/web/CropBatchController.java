package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.port.in.CreateCropBatchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crop-batches")
@RequiredArgsConstructor
public class CropBatchController {


    private final CreateCropBatchUseCase createCropBatchUseCase;

    @PostMapping
    public ResponseEntity<CropBatch> createCropBatch(@RequestBody CropBatch cropBatch) {
        CropBatch createdBatch = createCropBatchUseCase.create(cropBatch);
        return new ResponseEntity<>(createdBatch, HttpStatus.CREATED);
    }
}