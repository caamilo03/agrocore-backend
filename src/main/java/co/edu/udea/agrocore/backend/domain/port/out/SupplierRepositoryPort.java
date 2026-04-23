package co.edu.udea.agrocore.backend.domain.port.out;
import co.edu.udea.agrocore.backend.domain.model.Supplier;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierRepositoryPort {
    Supplier save(Supplier supplier);
    Optional<Supplier> findById(UUID id);
    List<Supplier> findAll();
    void deleteById(UUID id);
}