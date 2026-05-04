package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.domain.port.in.QueryTelemetryUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints de consulta de telemetria. Pensados para alimentar dashboards.
 *
 * Las cotas (default/max de limit) son defensivas: protegen al backend y a
 * la BD ante consultas accidentalmente enormes.
 */
@RestController
@RequestMapping("/api/v1/telemetry/batches/{batchId}")
public class TelemetryController {

    private static final int RECENT_DEFAULT_LIMIT = 100;
    private static final int RECENT_MAX_LIMIT = 1000;
    private static final int RANGE_MAX_LIMIT = 5000;

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

    /** Lecturas en [from, to] (ASC). Cap defensivo de 5000 elementos. */
    @GetMapping("/range")
    public ResponseEntity<List<TelemetryReading>> range(
            @PathVariable UUID batchId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(queryTelemetryUseCase.getInRange(batchId, from, to, RANGE_MAX_LIMIT));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
