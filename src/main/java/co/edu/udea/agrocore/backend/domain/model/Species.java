package co.edu.udea.agrocore.backend.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Species {
    private UUID idSpecies;
    private String name;
    private BigDecimal minTemperature;
    private BigDecimal maxTemperature;
    private BigDecimal minHumidity;
    private BigDecimal maxHumidity;
    private BigDecimal minCo2;
    private BigDecimal maxCo2;
}