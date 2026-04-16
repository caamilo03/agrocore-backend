package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "species")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeciesEntity {

    @Id
    @Column(name = "id_species")
    private UUID idSpecies;

    private String name;

    @Column(name = "min_temperature")
    private BigDecimal minTemperature;

    @Column(name = "max_temperature")
    private BigDecimal maxTemperature;

    @Column(name = "min_humidity")
    private BigDecimal minHumidity;

    @Column(name = "max_humidity")
    private BigDecimal maxHumidity;

    @Column(name = "min_co2")
    private BigDecimal minCo2;

    @Column(name = "max_co2")
    private BigDecimal maxCo2;
}