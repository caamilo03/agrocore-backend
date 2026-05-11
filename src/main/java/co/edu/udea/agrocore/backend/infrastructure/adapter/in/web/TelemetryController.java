package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.in.QueryTelemetryUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints de consulta de telemetria. Pensados para alimentar dashboards.
 *
 * Los parametros from/to del /range se reciben como OffsetDateTime (ISO 8601
 * con offset, ej. 2026-05-03T12:00:00Z) y se convierten a Instant para el
 * caso de uso. Asi forzamos al cliente a declarar la zona y evitamos
 * ambiguedades como las que motivaron migrar el dominio a Instant.
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
     * (bucket por hora si el rango es <= 7 dias, por dia en otro caso). Esto
     * garantiza que la respuesta cubra todo el periodo solicitado en lugar
     * de gastarse en las primeras horas. Cap defensivo de 5000 buckets.
     */
    @GetMapping("/range")
    public ResponseEntity<List<TelemetryReading>> range(
            @PathVariable UUID batchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to) {
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().build();
        }
        Instant fromInstant = from.toInstant();
        Instant toInstant = to.toInstant();
        return ResponseEntity.ok(
                queryTelemetryUseCase.getRepresentativeInRange(batchId, fromInstant, toInstant, RANGE_MAX_BUCKETS));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
