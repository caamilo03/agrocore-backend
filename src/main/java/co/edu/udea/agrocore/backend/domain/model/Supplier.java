package co.edu.udea.agrocore.backend.domain.model;

import java.util.UUID;

public class Supplier {
    private UUID idSupplier;
    private String nameSupplier;
    private String contactInfo;

    public Supplier() {}

    public Supplier(UUID idSupplier, String nameSupplier, String contactInfo) {
        this.idSupplier = idSupplier;
        this.nameSupplier = nameSupplier;
        this.contactInfo = contactInfo;
    }

    public UUID getIdSupplier() { return idSupplier; }
    public void setIdSupplier(UUID idSupplier) { this.idSupplier = idSupplier; }
    public String getNameSupplier() { return nameSupplier; }
    public void setNameSupplier(String nameSupplier) { this.nameSupplier = nameSupplier; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
}