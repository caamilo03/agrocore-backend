package co.edu.udea.agrocore.backend.application.exception;

/** Se lanza cuando el usuario autenticado no tiene permiso sobre el recurso solicitado. Mapea a HTTP 403. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}
