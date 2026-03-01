package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "crop_batch") // Debe coincidir con el nombre de la tabla en Flyway .sql
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropBatchEntity {

    @Id
    private UUID id;
    private String substrateOrigin;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
}