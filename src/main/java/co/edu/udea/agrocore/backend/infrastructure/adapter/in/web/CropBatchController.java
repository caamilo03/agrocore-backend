package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.port.in.*;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto.HarvestRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/batches")
public class CropBatchController {

    private final CreateCropBatchUseCase createCropBatchUseCase;
    private final GetAllCropBatchUseCase getAllCropBatchUseCase;
    private final UpdateCropBatchUseCase updateCropBatchUseCase;
    private final DeleteCropBatchUseCase deleteCropBatchUseCase;
    private final HarvestCropBatchUseCase harvestCropBatchUseCase;

    public CropBatchController(CreateCropBatchUseCase createCropBatchUseCase,
                               GetAllCropBatchUseCase getAllCropBatchUseCase,
                               UpdateCropBatchUseCase updateCropBatchUseCase,
                               DeleteCropBatchUseCase deleteCropBatchUseCase,
                               HarvestCropBatchUseCase harvestCropBatchUseCase) {
        this.createCropBatchUseCase = createCropBatchUseCase;
        this.getAllCropBatchUseCase = getAllCropBatchUseCase;
        this.updateCropBatchUseCase = updateCropBatchUseCase;
        this.deleteCropBatchUseCase = deleteCropBatchUseCase;
        this.harvestCropBatchUseCase = harvestCropBatchUseCase;
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

    /**
     * Marca un lote como cosechado.
     *
     * Respuestas:
     * <ul>
     *   <li>200 OK: lote actualizado.</li>
     *   <li>400 Bad Request: yieldKg ausente/&lt;=0 o endDate con formato invalido.</li>
     *   <li>404 Not Found: el lote no existe.</li>
     *   <li>409 Conflict: el lote no esta en estado ACTIVO (ya COSECHADO o PERDIDO).</li>
     * </ul>
     */
    @PostMapping("/{id}/harvest")
    public ResponseEntity<CropBatch> harvest(@PathVariable UUID id, @RequestBody HarvestRequest request) {
        Instant endDate = parseEndDate(request.endDate());
        return ResponseEntity.ok(harvestCropBatchUseCase.harvest(id, request.yieldKg(), endDate));
    }

    /**
     * Parsea el endDate opcional del body tolerando con y sin offset.
     * Mismo contrato que {@code TelemetryController.parseInstant} para que
     * el frontend pueda enviar timestamps uniformemente.
     *
     * @return el Instant parseado, o null si la entrada es null/blank.
     * @throws IllegalArgumentException si el string no se puede parsear.
     */
    static Instant parseEndDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException ignored) {
            // sin zona — fallback a UTC
        }
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("endDate '" + value + "' no es un ISO 8601 valido", ex);
        }
    }
}
