package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence;

import co.edu.udea.agrocore.backend.domain.model.User;
import co.edu.udea.agrocore.backend.domain.port.out.UserRepositoryPort;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.UserEntity;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final JpaUserRepository jpaRepository;

    public UserPersistenceAdapter(JpaUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public User save(User user) {
        return toDomain(jpaRepository.save(toEntity(user)));
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByGoogleId(String googleId) {
        return jpaRepository.findByIdGoogle(googleId).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    private UserEntity toEntity(User domain) {
        return UserEntity.builder()
                .id(domain.getId())
                .idGoogle(domain.getIdGoogle())
                .username(domain.getUsername())
                .email(domain.getEmail())
                .avatarUrl(domain.getAvatarUrl())
                .role(domain.getRole())
                .build();
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .idGoogle(entity.getIdGoogle())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .avatarUrl(entity.getAvatarUrl())
                .role(entity.getRole())
                .build();
    }
}
