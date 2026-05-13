package co.edu.udea.agrocore.backend.application.exception;

/**
 * Se lanza cuando una transicion de estado sobre un crop_batch es invalida
 * (por ejemplo, intentar cosechar un lote que ya esta COSECHADO o PERDIDO).
 * El GlobalExceptionHandler la mapea a HTTP 409 Conflict.
 */
public class InvalidBatchStateException extends RuntimeException {
    public InvalidBatchStateException(String message) {
        super(message);
    }
}
