package co.edu.udea.agrocore.backend.domain.model;

/**
 * Ciclo de vida de un crop_batch.
 *
 * <ul>
 *   <li>{@link #ACTIVO}: en cultivo, el simulador y los sensores reales
 *       generan telemetria para este lote.</li>
 *   <li>{@link #COSECHADO}: cultivo finalizado con exito, se registro el
 *       {@code yieldKg} y el {@code endDate}.</li>
 *   <li>{@link #PERDIDO}: cultivo terminado sin cosecha (perdida del lote).
 *       No transiciona a COSECHADO.</li>
 * </ul>
 *
 * Persistido como String (EnumType.STRING) para mantener compatibilidad
 * con la columna {@code crop_batch.status VARCHAR(50)} sin migracion.
 */
public enum CropBatchStatus {
    ACTIVO,
    COSECHADO,
    PERDIDO;

    /**
     * Parsea un string al enum tolerando mayusculas/minusculas.
     * Util para query params y bodies provenientes del frontend.
     *
     * @throws IllegalArgumentException si el valor es null, vacio o no
     *         corresponde a ningun status conocido. El mensaje incluye
     *         los valores aceptados para facilitar diagnostico.
     */
    public static CropBatchStatus fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(
                    "status no puede ser null o vacio. Valores aceptados: ACTIVO, COSECHADO, PERDIDO");
        }
        try {
            return CropBatchStatus.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "status '" + raw + "' no es valido. Valores aceptados: ACTIVO, COSECHADO, PERDIDO");
        }
    }
}
