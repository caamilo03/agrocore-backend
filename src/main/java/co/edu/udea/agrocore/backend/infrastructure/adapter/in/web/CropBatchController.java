package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.port.in.CreateCropBatchUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.GetCropBatchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crop-batches")
@RequiredArgsConstructor
public class CropBatchController {


    private final CreateCropBatchUseCase createCropBatchUseCase;
    private final GetCropBatchUseCase getCropBatchUseCase;

    @PostMapping
    public ResponseEntity<CropBatch> createCropBatch(@RequestBody CropBatch cropBatch) {
        CropBatch createdBatch = createCropBatchUseCase.create(cropBatch);
        return new ResponseEntity<>(createdBatch, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CropBatch>> getAllCropBatches() {
        List<CropBatch> batches = getCropBatchUseCase.getAll();
        return new ResponseEntity<>(batches, HttpStatus.OK);
    }
}