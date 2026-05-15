package co.edu.udea.agrocore.backend.domain.model;

import java.math.BigDecimal;

/**
 * Rangos optimos de las tres variables monitoreadas por la telemetria.
 * Tipicamente se construye desde {@link Species}, pero es independiente
 * para poder testear el calculo de stats sin acoplar al modelo de
 * especies.
 *
 * Todos los campos son obligatorios — si alguna especie tiene rangos
 * incompletos, debe rechazarse antes de invocar el calculo.
 */
public record OptimalRanges(
        BigDecimal minTemperature,
        BigDecimal maxTemperature,
        BigDecimal minHumidity,
        BigDecimal maxHumidity,
        BigDecimal minCo2,
        BigDecimal maxCo2
) {
    public OptimalRanges {
        if (minTemperature == null || maxTemperature == null
                || minHumidity == null || maxHumidity == null
                || minCo2 == null || maxCo2 == null) {
            throw new IllegalArgumentException("Todos los rangos optimos son obligatorios");
        }
    }
}
