-- Cambia telemetry_reading.recorded_at de TIMESTAMP (sin zona) a TIMESTAMPTZ.
--
-- Las filas existentes fueron escritas con LocalDateTime.now(clock) en UTC
-- (el VPS de Coolify corre en UTC), asi que reinterpretamos los valores
-- existentes asumiendo UTC. Esto preserva los instantes exactos: lo que
-- antes era "2026-05-05 15:49:46" ahora es "2026-05-05 15:49:46+00".
--
-- Tras esta migracion, Hibernate mapea Instant <-> TIMESTAMPTZ de forma
-- natural y Jackson serializa con el marcador 'Z'.

ALTER TABLE telemetry_reading
    ALTER COLUMN recorded_at TYPE TIMESTAMP WITH TIME ZONE
    USING recorded_at AT TIME ZONE 'UTC';
