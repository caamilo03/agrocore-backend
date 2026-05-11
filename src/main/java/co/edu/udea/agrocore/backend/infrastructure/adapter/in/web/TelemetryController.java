package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.in.QueryTelemetryUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints de consulta de telemetria. Pensados para alimentar dashboards.
 *
 * Los parametros from/to del /range se reciben como String y se parsean con
 * tolerancia: primero se intenta OffsetDateTime (ISO 8601 con zona, ej.
 * "2026-05-03T12:00:00Z"), y si falla se interpreta como LocalDateTime
 * asumiendo UTC (ej. "2026-05-03T12:00:00" enviado por clientes legacy).
 * Esto mantiene la compatibilidad con el frontend sin sacrificar la
 * precision de zona en clientes nuevos.
 *
 * Las cotas (default/max de limit) son defensivas: protegen al backend y a
 * la BD ante consultas accidentalmente enormes.
 */
@RestController
@RequestMapping("/api/v1/telemetry/batches/{batchId}")
public class TelemetryController {

    private static final int RECENT_DEFAULT_LIMIT = 100;
    private static final int RECENT_MAX_LIMIT = 1000;
    /**
     * Cap defensivo de buckets de downsampling para /range. Con granularidad
     * horaria cubre 208 dias; con diaria, ~13 anios. Nunca se gasta en las
     * primeras horas del rango como pasaba con el LIMIT plano anterior.
     */
    private static final int RANGE_MAX_BUCKETS = 5000;

    private final QueryTelemetryUseCase queryTelemetryUseCase;

    public TelemetryController(QueryTelemetryUseCase queryTelemetryUseCase) {
        this.queryTelemetryUseCase = queryTelemetryUseCase;
    }

    /** Ultima lectura del lote. 404 si aun no hay lecturas registradas. */
    @GetMapping("/latest")
    public ResponseEntity<TelemetryReading> latest(@PathVariable UUID batchId) {
        return queryTelemetryUseCase.getLatest(batchId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Ultimas N lecturas (DESC). limit default 100, max 1000. */
    @GetMapping("/recent")
    public ResponseEntity<List<TelemetryReading>> recent(
            @PathVariable UUID batchId,
            @RequestParam(defaultValue = "" + RECENT_DEFAULT_LIMIT) int limit) {
        int effectiveLimit = clamp(limit, 1, RECENT_MAX_LIMIT);
        return ResponseEntity.ok(queryTelemetryUseCase.getRecent(batchId, effectiveLimit));
    }

    /**
     * Lecturas representativas en [from, to] (ASC), con downsampling temporal
     * (bucket por hora si el rango es <= 7 dias, por dia en otro caso). Cubre
     * todo el periodo solicitado. Cap defensivo de 5000 buckets.
     *
     * Los parametros from/to aceptan tanto ISO 8601 con zona (2026-05-03T12:00:00Z)
     * como sin zona (2026-05-03T12:00:00); en este ultimo caso se asume UTC.
     */
    @GetMapping("/range")
    public ResponseEntity<List<TelemetryReading>> range(
            @PathVariable UUID batchId,
            @RequestParam String from,
            @RequestParam String to) {
        Instant fromInstant = parseInstant(from);
        Instant toInstant = parseInstant(to);
        if (fromInstant == null || toInstant == null) {
            return ResponseEntity.badRequest().build();
        }
        if (fromInstant.isAfter(toInstant)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(
                queryTelemetryUseCase.getRepresentativeInRange(batchId, fromInstant, toInstant, RANGE_MAX_BUCKETS));
    }

    /**
     * Parsea un string de fecha/hora a Instant tolerando dos formatos:
     * - Con zona: "2026-05-03T12:00:00Z" o "2026-05-03T12:00:00+05:00"
     * - Sin zona: "2026-05-03T12:00:00" (se asume UTC)
     * Devuelve null si el formato no es reconocido.
     */
    static Instant parseInstant(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException ignored) {
            // no tiene zona — intentar como LocalDateTime y asumir UTC
        }
        try {
            return LocalDateTime.parse(value).toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
