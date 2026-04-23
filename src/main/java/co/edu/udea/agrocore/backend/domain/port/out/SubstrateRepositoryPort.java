package co.edu.udea.agrocore.backend.domain.port.out;
import co.edu.udea.agrocore.backend.domain.model.Substrate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubstrateRepositoryPort {
    Substrate save(Substrate substrate);
    Optional<Substrate> findById(UUID id);
    List<Substrate> findAll();
    void deleteById(UUID id);
}