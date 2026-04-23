package co.edu.udea.agrocore.backend.domain.port.in;
import java.util.UUID;

public interface DeleteSubstrateUseCase {
    void delete(UUID id);
}