-- Indice para consultas tipicas de telemetria por lote y rango temporal
-- (ej. ultimas N lecturas de un crop_batch ordenadas DESC).
CREATE INDEX IF NOT EXISTS idx_telemetry_batch_time
    ON telemetry_reading (id_crop_batch, recorded_at DESC);
