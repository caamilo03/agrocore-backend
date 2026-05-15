package co.edu.udea.agrocore.backend.domain.port.out;

import co.edu.udea.agrocore.backend.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    List<User> findAll();
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
