package co.edu.udea.agrocore.backend.application.exception;

/**
 * Se lanza cuando el Google ID token es inválido, expirado o no se puede
 * verificar. GlobalExceptionHandler lo mapea a HTTP 401 Unauthorized.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
