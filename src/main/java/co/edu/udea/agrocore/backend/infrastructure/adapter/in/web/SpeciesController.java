package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.Species;
import co.edu.udea.agrocore.backend.domain.port.in.CreateSpeciesUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.DeleteSpeciesUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.GetAllSpeciesUseCase;
import co.edu.udea.agrocore.backend.domain.port.in.UpdateSpeciesUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/species")
public class SpeciesController {

    private final CreateSpeciesUseCase createSpeciesUseCase;
    private final GetAllSpeciesUseCase getAllSpeciesUseCase;
    private final UpdateSpeciesUseCase updateSpeciesUseCase;
    private final DeleteSpeciesUseCase deleteSpeciesUseCase;

    public SpeciesController(CreateSpeciesUseCase createSpeciesUseCase,
                             GetAllSpeciesUseCase getAllSpeciesUseCase,
                             UpdateSpeciesUseCase updateSpeciesUseCase,
                             DeleteSpeciesUseCase deleteSpeciesUseCase) {
        this.createSpeciesUseCase = createSpeciesUseCase;
        this.getAllSpeciesUseCase = getAllSpeciesUseCase;
        this.updateSpeciesUseCase = updateSpeciesUseCase;
        this.deleteSpeciesUseCase = deleteSpeciesUseCase;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Species> create(@RequestBody Species species) {
        return ResponseEntity.ok(createSpeciesUseCase.create(species));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Species>> getAll() {
        return ResponseEntity.ok(getAllSpeciesUseCase.getAll());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Species> update(@PathVariable UUID id, @RequestBody Species species) {
        return ResponseEntity.ok(updateSpeciesUseCase.update(id, species));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteSpeciesUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
