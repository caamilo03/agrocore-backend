package co.edu.udea.agrocore.backend.infrastructure.adapter.in.web;

import co.edu.udea.agrocore.backend.application.exception.InvalidBatchStateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Mapea excepciones de dominio/aplicacion a respuestas HTTP semanticas.
 *
 * - {@link InvalidBatchStateException} -> 409 Conflict
 * - {@link NoSuchElementException}     -> 404 Not Found
 * - {@link IllegalArgumentException}   -> 400 Bad Request
 *
 * Devuelve un cuerpo JSON minimo `{ "error": "..." }` para que el frontend
 * pueda mostrar mensajes al usuario sin parsear el shape default de Spring.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidBatchStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidBatchState(InvalidBatchStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}
