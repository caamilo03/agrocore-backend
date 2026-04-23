package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
public class SupplierEntity {

    @Id
    @Column(name = "id_supplier")
    private UUID idSupplier;

    @Column(name = "name_supplier", nullable = false, length = 100)
    private String nameSupplier;

    @Column(name = "contact_info", length = 255)
    private String contactInfo;

    public SupplierEntity() {}

    public UUID getIdSupplier() { return idSupplier; }
    public void setIdSupplier(UUID idSupplier) { this.idSupplier = idSupplier; }
    public String getNameSupplier() { return nameSupplier; }
    public void setNameSupplier(String nameSupplier) { this.nameSupplier = nameSupplier; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
}