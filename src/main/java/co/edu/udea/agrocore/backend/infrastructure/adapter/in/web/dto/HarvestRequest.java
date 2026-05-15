package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * Body del endpoint POST /api/v1/batches/{id}/harvest.
 *
 * - {@code yieldKg}: peso obtenido en kilos (obligatorio, debe ser &gt; 0).
 * - {@code endDate}: fecha/hora de finalizacion del cultivo (opcional, ISO 8601).
 *   Acepta tanto formato con zona ("2026-05-13T15:00:00Z") como sin zona
 *   ("2026-05-13T15:00:00", asumido UTC). Si se omite, se usa el instante
 *   actual del servidor.
 */
public record HarvestRequest(BigDecimal yieldKg, String endDate) {
}
