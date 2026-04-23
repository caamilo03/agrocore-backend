package co.edu.udea.agrocore.backend.domain.port.in;
import co.edu.udea.agrocore.backend.domain.model.Substrate;

public interface CreateSubstrateUseCase {
    Substrate create(Substrate substrate);
}