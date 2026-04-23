package co.edu.udea.agrocore.backend.application.service;

import co.edu.udea.agrocore.backend.domain.model.Supplier;
import co.edu.udea.agrocore.backend.domain.port.in.*;
import co.edu.udea.agrocore.backend.domain.port.out.SupplierRepositoryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SupplierService implements CreateSupplierUseCase, UpdateSupplierUseCase, GetAllSuppliersUseCase, DeleteSupplierUseCase {

    private final SupplierRepositoryPort supplierRepositoryPort;

    public SupplierService(SupplierRepositoryPort supplierRepositoryPort) {
        this.supplierRepositoryPort = supplierRepositoryPort;
    }

    @Override
    public Supplier create(Supplier supplier) {
        if (supplier.getIdSupplier() == null) {
            supplier.setIdSupplier(UUID.randomUUID());
        }
        return supplierRepositoryPort.save(supplier);
    }

    @Override
    public Supplier update(UUID id, Supplier supplier) {
        Supplier existingSupplier = supplierRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        existingSupplier.setNameSupplier(supplier.getNameSupplier());
        existingSupplier.setContactInfo(supplier.getContactInfo());

        return supplierRepositoryPort.save(existingSupplier);
    }

    @Override
    public List<Supplier> getAll() {
        return supplierRepositoryPort.findAll();
    }

    @Override
    public void delete(UUID id) {
        supplierRepositoryPort.deleteById(id);
    }
}