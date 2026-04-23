package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.Substrate;
import co.edu.udea.agrocore.backend.domain.port.in.*;
import co.edu.udea.agrocore.backend.domain.port.out.SubstrateRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SubstrateService implements CreateSubstrateUseCase, UpdateSubstrateUseCase, GetAllSubstratesUseCase, DeleteSubstrateUseCase {

    private final SubstrateRepositoryPort substrateRepositoryPort;

    public SubstrateService(SubstrateRepositoryPort substrateRepositoryPort) {
        this.substrateRepositoryPort = substrateRepositoryPort;
    }

    @Override
    public Substrate create(Substrate substrate) {
        if (substrate.getIdSubstrate() == null) {
            substrate.setIdSubstrate(UUID.randomUUID());
        }
        return substrateRepositoryPort.save(substrate);
    }

    @Override
    public Substrate update(UUID id, Substrate substrate) {
        Substrate existingSubstrate = substrateRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Sustrato no encontrado"));

        existingSubstrate.setTypeName(substrate.getTypeName());
        existingSubstrate.setDescription(substrate.getDescription());

        return substrateRepositoryPort.save(existingSubstrate);
    }

    @Override
    public List<Substrate> getAll() {
        return substrateRepositoryPort.findAll();
    }

    @Override
    public void delete(UUID id) {
        substrateRepositoryPort.deleteById(id);
    }
}