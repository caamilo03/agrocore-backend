package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "crop_batch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropBatchEntity {

    @Id
    @Column(name = "id_crop_batch")
    private UUID id;

    @Column(name = "id_species")
    private UUID idSpecies;

    @Column(name = "id_substrate")
    private UUID idSubstrate;

    @Column(name = "id_species_supplier")
    private UUID idSpeciesSupplier;

    @Column(name = "id_substrate_supplier")
    private UUID idSubstrateSupplier;

    @Column(name = "id_user")
    private UUID idUser;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    private String status;

    @Column(name = "yield_kg")
    private BigDecimal yieldKg;
}