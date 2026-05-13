package co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence;

import co.edu.udea.agrocore.backend.domain.model.TelemetryReading;
import co.edu.udea.agrocore.backend.infrastructure.adapter.out.persistence.repository.JpaTelemetryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cubre el downsampling temporal del adapter: eleccion de granularidad
 * (hour vs day) y mapeo de la salida del query nativo a TelemetryReading.
 */
class TelemetryPersistenceAdapterTest {

    private static final UUID BATCH_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    private JpaTelemetryRepository jpaRepository;
    private TelemetryPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(JpaTelemetryRepository.class);
        adapter = new TelemetryPersistenceAdapter(jpaRepository);
    }

    @Test
    void findRepresentativeInRange_shortRangeUsesHourlyGranularity_returnsAllReadingsWhenSparse() {
        // Caso 1: 24h con pocas lecturas. Granularidad = hora. Si solo hay 3
        // buckets con datos, el adapter devuelve esos 3 readings.
        Instant from = Instant.parse("2026-05-05T00:00:00Z");
        Instant to = from.plus(Duration.ofHours(24));

        List<Object[]> rows = new ArrayList<>();
        rows.add(row("2026-05-05T01:30:00Z", "22.50", "65.00", "420.00"));
        rows.add(row("2026-05-05T09:15:00Z", "24.10", "63.20", "415.50"));
        rows.add(row("2026-05-05T18:45:00Z", "21.80", "67.40", "430.00"));

        when(jpaRepository.findRepresentativeBuckets(eq(BATCH_ID), eq(from), eq(to), eq("hour"), eq(5000)))
                .thenReturn(rows);

        List<TelemetryReading> result = adapter.findRepresentativeInRange(BATCH_ID, from, to, 5000);

        verify(jpaRepository).findRepresentativeBuckets(BATCH_ID, from, to, "hour", 5000);
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRecordedAt()).isEqualTo(Instant.parse("2026-05-05T01:30:00Z"));
        assertThat(result.get(0).getIdCropBatch()).isEqualTo(BATCH_ID);
        assertThat(result.get(0).getId()).isNull(); // agregado, no fila real
        assertThat(result.get(0).getTemperature()).isEqualByComparingTo("22.50");
        assertThat(result.get(2).getCo2()).isEqualByComparingTo("430.00");
        // Todas las lecturas caen dentro del rango pedido.
        assertThat(result).allSatisfy(r ->
                assertThat(r.getRecordedAt()).isBetween(from, to));
    }

    @Test
    void findRepresentativeInRange_longRangeUsesDailyGranularity_returnsBucketsDistributedUniformly() {
        // Caso 2: 30 dias con muchas lecturas. Granularidad = dia.
        // Simulamos 30 buckets, uno por dia.
        Instant from = Instant.parse("2026-04-01T00:00:00Z");
        Instant to = from.plus(Duration.ofDays(30));

        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Instant bucketAt = from.plus(Duration.ofDays(i)).plus(Duration.ofHours(12));
            rows.add(row(bucketAt.toString(), "22.00", "65.00", "420.00"));
        }

        when(jpaRepository.findRepresentativeBuckets(eq(BATCH_ID), eq(from), eq(to), eq("day"), eq(5000)))
                .thenReturn(rows);

        List<TelemetryReading> result = adapter.findRepresentativeInRange(BATCH_ID, from, to, 5000);

        verify(jpaRepository).findRepresentativeBuckets(BATCH_ID, from, to, "day", 5000);
        assertThat(result).hasSize(30);
        // Verificacion de distribucion: la diferencia entre buckets consecutivos
        // es ~1 dia (no se concentran en las primeras horas).
        for (int i = 1; i < result.size(); i++) {
            Duration gap = Duration.between(result.get(i - 1).getRecordedAt(), result.get(i).getRecordedAt());
            assertThat(gap.toHours()).isBetween(20L, 28L);
        }
        // Buckets cubren todo el periodo: primero cerca del inicio, ultimo cerca del fin.
        assertThat(result.get(0).getRecordedAt()).isBefore(from.plus(Duration.ofDays(2)));
        assertThat(result.get(result.size() - 1).getRecordedAt()).isAfter(to.minus(Duration.ofDays(2)));
    }

    @Test
    void findRepresentativeInRange_sevenDaysExactlyStaysOnHourly() {
        // Borde: exactamente 7 dias debe seguir siendo 'hour' (umbral es > 7).
        Instant from = Instant.parse("2026-05-01T00:00:00Z");
        Instant to = from.plus(Duration.ofDays(7));
        when(jpaRepository.findRepresentativeBuckets(any(), any(), any(), any(), anyInt()))
                .thenReturn(List.of());

        adapter.findRepresentativeInRange(BATCH_ID, from, to, 5000);

        verify(jpaRepository).findRepresentativeBuckets(BATCH_ID, from, to, "hour", 5000);
    }

    @Test
    void toInstantMapping_supportsTimestampOffsetDateTimeAndInstant() {
        // pgjdbc puede devolver cualquiera de los tres segun version/driver.
        Instant expected = Instant.parse("2026-05-05T10:00:00Z");

        Object[] timestampRow = row(Timestamp.from(expected), bd("22.00"), bd("65.00"), bd("420.00"));
        Object[] odtRow = row(expected.atOffset(ZoneOffset.UTC), bd("22.00"), bd("65.00"), bd("420.00"));
        Object[] instantRow = row(expected, bd("22.00"), bd("65.00"), bd("420.00"));

        Instant from = Instant.parse("2026-05-01T00:00:00Z");
        Instant to = Instant.parse("2026-05-08T00:00:00Z");

        for (Object[] r : List.of(timestampRow, odtRow, instantRow)) {
            when(jpaRepository.findRepresentativeBuckets(any(), any(), any(), any(), anyInt()))
                    .thenReturn(List.<Object[]>of(r));
            List<TelemetryReading> result = adapter.findRepresentativeInRange(BATCH_ID, from, to, 5000);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRecordedAt()).isEqualTo(expected);
        }
    }

    // -- computeStats --

    @org.junit.jupiter.api.Test
    void computeStats_mapsAllAggregates() {
        co.edu.udea.agrocore.backend.domain.model.OptimalRanges ranges =
                new co.edu.udea.agrocore.backend.domain.model.OptimalRanges(
                        bd("18.00"), bd("28.00"),
                        bd("60.00"), bd("80.00"),
                        bd("350.00"), bd("600.00"));
        Object[] sqlRow = new Object[]{
                86400L,
                bd("22.4"), bd("14.1"), bd("31.2"), bd("87.5"),
                bd("68.3"), bd("42.0"), bd("91.0"), bd("76.2"),
                bd("480.5"), bd("320.0"), bd("720.0"), bd("91.0")
        };
        when(jpaRepository.computeStatsRow(
                eq(BATCH_ID), any(), any(),
                eq(bd("18.00")), eq(bd("28.00")),
                eq(bd("60.00")), eq(bd("80.00")),
                eq(bd("350.00")), eq(bd("600.00")))).thenReturn(sqlRow);

        Instant from = Instant.parse("2026-04-01T00:00:00Z");
        Instant to = Instant.parse("2026-05-01T00:00:00Z");
        co.edu.udea.agrocore.backend.domain.model.TelemetryStats stats =
                adapter.computeStats(BATCH_ID, from, to, ranges);

        assertThat(stats.count()).isEqualTo(86400L);
        assertThat(stats.avgTemperature()).isEqualByComparingTo("22.40");
        assertThat(stats.minTemperature()).isEqualByComparingTo("14.10");
        assertThat(stats.maxTemperature()).isEqualByComparingTo("31.20");
        assertThat(stats.temperatureInRangePct()).isEqualByComparingTo("87.50");
        assertThat(stats.avgHumidity()).isEqualByComparingTo("68.30");
        assertThat(stats.humidityInRangePct()).isEqualByComparingTo("76.20");
        assertThat(stats.co2InRangePct()).isEqualByComparingTo("91.00");
    }

    @org.junit.jupiter.api.Test
    void computeStats_returnsEmptyWhenCountIsZero() {
        co.edu.udea.agrocore.backend.domain.model.OptimalRanges ranges =
                new co.edu.udea.agrocore.backend.domain.model.OptimalRanges(
                        bd("18.00"), bd("28.00"),
                        bd("60.00"), bd("80.00"),
                        bd("350.00"), bd("600.00"));
        Object[] sqlRow = new Object[]{0L, null, null, null, null, null, null, null, null, null, null, null, null};
        when(jpaRepository.computeStatsRow(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(sqlRow);

        co.edu.udea.agrocore.backend.domain.model.TelemetryStats stats =
                adapter.computeStats(BATCH_ID, Instant.now(), Instant.now(), ranges);

        assertThat(stats.count()).isZero();
        assertThat(stats.avgTemperature()).isNull();
        assertThat(stats.temperatureInRangePct()).isNull();
    }

    @org.junit.jupiter.api.Test
    void computeStats_handlesWrappedRowFromPgjdbc() {
        // Algunas versiones de Hibernate envuelven el row en Object[1].
        co.edu.udea.agrocore.backend.domain.model.OptimalRanges ranges =
                new co.edu.udea.agrocore.backend.domain.model.OptimalRanges(
                        bd("18.00"), bd("28.00"),
                        bd("60.00"), bd("80.00"),
                        bd("350.00"), bd("600.00"));
        Object[] innerRow = new Object[]{
                10L,
                bd("22.0"), bd("20.0"), bd("24.0"), bd("100.0"),
                bd("65.0"), bd("60.0"), bd("70.0"), bd("100.0"),
                bd("400.0"), bd("380.0"), bd("420.0"), bd("100.0")
        };
        when(jpaRepository.computeStatsRow(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new Object[]{innerRow});

        co.edu.udea.agrocore.backend.domain.model.TelemetryStats stats =
                adapter.computeStats(BATCH_ID, Instant.now(), Instant.now(), ranges);

        assertThat(stats.count()).isEqualTo(10L);
        assertThat(stats.avgTemperature()).isEqualByComparingTo("22.00");
    }

    // -- helpers --

    private static Object[] row(String iso, String temp, String hum, String co2) {
        return row(OffsetDateTime.parse(iso), bd(temp), bd(hum), bd(co2));
    }

    private static Object[] row(Object bucketAt, BigDecimal temp, BigDecimal hum, BigDecimal co2) {
        return new Object[]{bucketAt, temp, hum, co2};
    }

    private static BigDecimal bd(String s) {
        return new BigDecimal(s);
    }
}
