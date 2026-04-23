package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "substrates")
public class SubstrateEntity {

    @Id
    @Column(name = "id_substrate")
    private UUID idSubstrate;

    @Column(name = "type_name", nullable = false, length = 100)
    private String typeName;

    @Column(name = "description", length = 255)
    private String description;

    public SubstrateEntity() {}

    public UUID getIdSubstrate() { return idSubstrate; }
    public void setIdSubstrate(UUID idSubstrate) { this.idSubstrate = idSubstrate; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}