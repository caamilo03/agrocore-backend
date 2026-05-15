package co.edu.udea.agrocore.backend.domain.model;

/**
 * Roles del sistema AgroCore.
 *
 * <ul>
 *   <li>{@link #ADMIN}: acceso total — catalogs, users, todos los lotes.</li>
 *   <li>{@link #OPERADOR}: crea y gestiona sus propios lotes; lee catálogos.</li>
 *   <li>{@link #OBSERVADOR}: solo lectura — analítica, trazabilidad, telemetría.</li>
 * </ul>
 *
 * Persistido como String (EnumType.STRING) en la columna {@code users.role VARCHAR(50)}.
 */
public enum Role {
    ADMIN,
    OPERADOR,
    OBSERVADOR;

    /**
     * Parsea un string al enum tolerando mayusculas/minusculas y espacios.
     *
     * @throws IllegalArgumentException si el valor es null, vacio o desconocido.
     */
    public static Role fromString(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException(
                    "role no puede ser null o vacio. Valores aceptados: ADMIN, OPERADOR, OBSERVADOR");
        }
        try {
            return Role.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "role '" + raw + "' no es valido. Valores aceptados: ADMIN, OPERADOR, OBSERVADOR");
        }
    }
}
