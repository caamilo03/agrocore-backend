package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository;

import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity.SubstrateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaSubstrateRepository extends JpaRepository<SubstrateEntity, UUID> {
}