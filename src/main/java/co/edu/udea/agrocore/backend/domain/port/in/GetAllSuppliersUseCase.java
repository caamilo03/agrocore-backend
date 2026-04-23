package co.edu.udea.agrocore.backend.domain.port.in;
import co.edu.udea.agrocore.backend.domain.model.Supplier;
import java.util.List;

public interface GetAllSuppliersUseCase {
    List<Supplier> getAll();
}