package co.edu.udea.agrocore.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropBatch {
    private UUID id;
    private UUID idSpecies;
    private UUID idSubstrate;
    private UUID idSpeciesSupplier;
    private UUID idSubstrateSupplier;
    private UUID idUser;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private BigDecimal yieldKg;
}