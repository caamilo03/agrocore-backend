package co.edu.udea.agrocore.backend.domain.model;

import java.math.BigDecimal;

/**
 * Resumen estadistico de las lecturas de telemetria de un lote en un rango
 * temporal. Para cada variable (temperatura, humedad, CO2) se exponen:
 *
 * <ul>
 *   <li>{@code avg} — promedio aritmetico</li>
 *   <li>{@code min} / {@code max} — extremos observados</li>
 *   <li>{@code inRangePct} — porcentaje (0-100) de lecturas que cayeron
 *       dentro del rango optimo de la especie</li>
 * </ul>
 *
 * {@code count == 0} indica que no hubo lecturas en el rango; los demas
 * campos seran null en ese caso.
 */
public record TelemetryStats(
        long count,
        BigDecimal avgTemperature, BigDecimal minTemperature, BigDecimal maxTemperature, BigDecimal temperatureInRangePct,
        BigDecimal avgHumidity, BigDecimal minHumidity, BigDecimal maxHumidity, BigDecimal humidityInRangePct,
        BigDecimal avgCo2, BigDecimal minCo2, BigDecimal maxCo2, BigDecimal co2InRangePct
) {
    /** Constructor de conveniencia para rangos sin lecturas. */
    public static TelemetryStats empty() {
        return new TelemetryStats(0L, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
