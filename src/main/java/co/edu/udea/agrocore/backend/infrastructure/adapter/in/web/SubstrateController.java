package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.Substrate;
import co.edu.udea.agrocore.backend.domain.port.in.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/substrates")
//@CrossOrigin(origins = "*") // Ajusta esto según tus políticas de CORS
public class SubstrateController {

    private final CreateSubstrateUseCase createSubstrateUseCase;
    private final UpdateSubstrateUseCase updateSubstrateUseCase;
    private final GetAllSubstratesUseCase getAllSubstratesUseCase;
    private final DeleteSubstrateUseCase deleteSubstrateUseCase;

    public SubstrateController(CreateSubstrateUseCase createSubstrateUseCase,
                               UpdateSubstrateUseCase updateSubstrateUseCase,
                               GetAllSubstratesUseCase getAllSubstratesUseCase,
                               DeleteSubstrateUseCase deleteSubstrateUseCase) {
        this.createSubstrateUseCase = createSubstrateUseCase;
        this.updateSubstrateUseCase = updateSubstrateUseCase;
        this.getAllSubstratesUseCase = getAllSubstratesUseCase;
        this.deleteSubstrateUseCase = deleteSubstrateUseCase;
    }

    @PostMapping
    public ResponseEntity<Substrate> create(@RequestBody Substrate substrate) {
        Substrate created = createSubstrateUseCase.create(substrate);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Substrate>> getAll() {
        return ResponseEntity.ok(getAllSubstratesUseCase.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Substrate> update(@PathVariable UUID id, @RequestBody Substrate substrate) {
        return ResponseEntity.ok(updateSubstrateUseCase.update(id, substrate));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteSubstrateUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}