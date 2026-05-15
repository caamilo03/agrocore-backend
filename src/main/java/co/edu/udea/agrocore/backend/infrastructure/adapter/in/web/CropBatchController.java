package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.CropBatch;
import co.edu.udea.agrocore.backend.domain.model.CropBatchStatus;
import co.edu.udea.agrocore.backend.domain.port.in.*;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto.HarvestRequest;
import co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto.TraceabilityResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final GetCropBatchTraceabilityUseCase getTraceabilityUseCase;

    public CropBatchController(CreateCropBatchUseCase createCropBatchUseCase,
                               GetAllCropBatchUseCase getAllCropBatchUseCase,
                               UpdateCropBatchUseCase updateCropBatchUseCase,
                               DeleteCropBatchUseCase deleteCropBatchUseCase,
                               HarvestCropBatchUseCase harvestCropBatchUseCase,
                               GetCropBatchTraceabilityUseCase getTraceabilityUseCase) {
        this.createCropBatchUseCase = createCropBatchUseCase;
        this.getAllCropBatchUseCase = getAllCropBatchUseCase;
        this.updateCropBatchUseCase = updateCropBatchUseCase;
        this.deleteCropBatchUseCase = deleteCropBatchUseCase;
        this.harvestCropBatchUseCase = harvestCropBatchUseCase;
        this.getTraceabilityUseCase = getTraceabilityUseCase;
    }

    /** ADMIN y OPERADOR pueden crear lotes. OPERADOR solo crea lotes propios (el service lo fuerza). */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<CropBatch> create(@RequestBody CropBatch cropBatch) {
        return ResponseEntity.ok(createCropBatchUseCase.create(cropBatch));
    }

    /** Todos los autenticados pueden listar. El service filtra por usuario para OPERADOR. */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CropBatch>> getAll(@RequestParam(required = false) String status) {
        CropBatchStatus filter = (status == null || status.isBlank())
                ? null
                : CropBatchStatus.fromString(status);
        return ResponseEntity.ok(getAllCropBatchUseCase.getAll(filter));
    }

    /** ADMIN y OPERADOR pueden modificar. El service verifica ownership para OPERADOR. */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<CropBatch> update(@PathVariable UUID id, @RequestBody CropBatch cropBatch) {
        return ResponseEntity.ok(updateCropBatchUseCase.update(id, cropBatch));
    }

    /** ADMIN y OPERADOR pueden eliminar. El service verifica ownership para OPERADOR. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteCropBatchUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** ADMIN y OPERADOR pueden cosechar. El service verifica ownership para OPERADOR. */
    @PostMapping("/{id}/harvest")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<CropBatch> harvest(@PathVariable UUID id, @RequestBody HarvestRequest request) {
        Instant endDate = parseEndDate(request.endDate());
        return ResponseEntity.ok(harvestCropBatchUseCase.harvest(id, request.yieldKg(), endDate));
    }

    /** Trazabilidad: todos los autenticados pueden ver. El service verifica acceso para OPERADOR. */
    @GetMapping("/{id}/traceability")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TraceabilityResponse> traceability(@PathVariable UUID id) {
        return ResponseEntity.ok(TraceabilityResponse.from(getTraceabilityUseCase.get(id)));
    }

    static Instant parseEndDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException ignored) {
        }
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("endDate '" + value + "' no es un ISO 8601 valido", ex);
        }
    }
}
