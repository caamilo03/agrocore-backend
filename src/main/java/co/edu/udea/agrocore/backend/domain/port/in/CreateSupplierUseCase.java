package co.edu.udea.agrocore.backend.domain.port.in;
import co.edu.udea.agrocore.backend.domain.model.Supplier;

public interface CreateSupplierUseCase {
    Supplier create(Supplier supplier);
}