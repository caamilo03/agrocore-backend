package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.Supplier;
import co.edu.udea.agrocore.backend.domain.port.in.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/suppliers")
@CrossOrigin(origins = "*") // Ajusta según tus políticas de CORS
public class SupplierController {

    private final CreateSupplierUseCase createSupplierUseCase;
    private final UpdateSupplierUseCase updateSupplierUseCase;
    private final GetAllSuppliersUseCase getAllSuppliersUseCase;
    private final DeleteSupplierUseCase deleteSupplierUseCase;

    public SupplierController(CreateSupplierUseCase createSupplierUseCase,
                              UpdateSupplierUseCase updateSupplierUseCase,
                              GetAllSuppliersUseCase getAllSuppliersUseCase,
                              DeleteSupplierUseCase deleteSupplierUseCase) {
        this.createSupplierUseCase = createSupplierUseCase;
        this.updateSupplierUseCase = updateSupplierUseCase;
        this.getAllSuppliersUseCase = getAllSuppliersUseCase;
        this.deleteSupplierUseCase = deleteSupplierUseCase;
    }

    @PostMapping
    public ResponseEntity<Supplier> create(@RequestBody Supplier supplier) {
        Supplier created = createSupplierUseCase.create(supplier);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Supplier>> getAll() {
        return ResponseEntity.ok(getAllSuppliersUseCase.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> update(@PathVariable UUID id, @RequestBody Supplier supplier) {
        return ResponseEntity.ok(updateSupplierUseCase.update(id, supplier));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteSupplierUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}