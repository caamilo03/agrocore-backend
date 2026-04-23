package co.edu.udea.agrocore.backend.domain.port.in;
import co.edu.udea.agrocore.backend.domain.model.Substrate;
import java.util.List;

public interface GetAllSubstratesUseCase {
    List<Substrate> getAll();
}