package co.edu.udea.agrocore.backend.domain.port.in;
import co.edu.udea.agrocore.backend.domain.model.Substrate;
import java.util.UUID;

public interface UpdateSubstrateUseCase {
    Substrate update(UUID id, Substrate substrate);
}