package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.application.service.SpeciesService;
import co.edu.udea.agrocore.backend.domain.model.Species;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/species")
@CrossOrigin(origins = "*") // frontend de Next.js se pueda conectar sin tirar errores de CORS por ahora
public class SpeciesController {

    private final SpeciesService service;

    public SpeciesController(SpeciesService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Species> create(@RequestBody Species species) {
        return ResponseEntity.ok(service.createSpecies(species));
    }

    @GetMapping
    public ResponseEntity<List<Species>> getAll() {
        return ResponseEntity.ok(service.getAllSpecies());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Species> update(@PathVariable UUID id, @RequestBody Species species) {
        return ResponseEntity.ok(service.updateSpecies(id, species));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.deleteSpecies(id);
        return ResponseEntity.noContent().build();
    }
}