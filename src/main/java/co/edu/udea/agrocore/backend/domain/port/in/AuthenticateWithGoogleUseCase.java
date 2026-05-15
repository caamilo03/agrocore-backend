package co.edu.udea.agrocore.backend.domain.port.in;

public interface AuthenticateWithGoogleUseCase {
    AuthResult authenticate(String googleIdToken);
}
