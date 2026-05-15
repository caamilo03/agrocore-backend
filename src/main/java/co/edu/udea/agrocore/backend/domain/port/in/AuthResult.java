package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.User;

/**
 * Resultado del proceso de autenticacion con Google.
 *
 * @param jwt   JWT firmado por AgroCore listo para enviar al cliente.
 * @param user  Usuario creado o encontrado en la BD.
 */
public record AuthResult(String jwt, User user) {
}
