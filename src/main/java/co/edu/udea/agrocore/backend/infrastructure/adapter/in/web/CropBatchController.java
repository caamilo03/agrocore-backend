package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.port.in.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/batches")
//@CrossOrigin(origins = "*")
public class CropBatchController {

    private final CreateCropBatchUseCase createCropBatchUseCase;
    private final GetAllCropBatchUseCase getAllCropBatchUseCase;
    private final UpdateCropBatchUseCase updateCropBatchUseCase;
    private final DeleteCropBatchUseCase deleteCropBatchUseCase;

    public CropBatchController(CreateCropBatchUseCase createCropBatchUseCase,
                               GetAllCropBatchUseCase getAllCropBatchUseCase,
                               UpdateCropBatchUseCase updateCropBatchUseCase,
                               DeleteCropBatchUseCase deleteCropBatchUseCase) {
        this.createCropBatchUseCase = createCropBatchUseCase;
        this.getAllCropBatchUseCase = getAllCropBatchUseCase;
        this.updateCropBatchUseCase = updateCropBatchUseCase;
        this.deleteCropBatchUseCase = deleteCropBatchUseCase;
    }

    @PostMapping
    public ResponseEntity<CropBatch> create(@RequestBody CropBatch cropBatch) {
        return ResponseEntity.ok(createCropBatchUseCase.create(cropBatch));
    }

    @GetMapping
    public ResponseEntity<List<CropBatch>> getAll() {
        return ResponseEntity.ok(getAllCropBatchUseCase.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CropBatch> update(@PathVariable UUID id, @RequestBody CropBatch cropBatch) {
        return ResponseEntity.ok(updateCropBatchUseCase.update(id, cropBatch));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteCropBatchUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}