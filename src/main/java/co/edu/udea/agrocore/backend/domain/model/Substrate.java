package co.edu.udea.agrocore.backend.domain.model;

import java.util.UUID;

public class Substrate {
    private UUID idSubstrate;
    private String typeName;
    private String description;

    public Substrate() {}

    public Substrate(UUID idSubstrate, String typeName, String description) {
        this.idSubstrate = idSubstrate;
        this.typeName = typeName;
        this.description = description;
    }

    public UUID getIdSubstrate() { return idSubstrate; }
    public void setIdSubstrate(UUID idSubstrate) { this.idSubstrate = idSubstrate; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}