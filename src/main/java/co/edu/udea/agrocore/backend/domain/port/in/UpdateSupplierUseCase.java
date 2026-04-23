package co.edu.udea.agrocore.backend.domain.port.in;
import co.edu.udea.agrocore.backend.domain.model.Supplier;
import java.util.UUID;

public interface UpdateSupplierUseCase {
    Supplier update(UUID id, Supplier supplier);
}