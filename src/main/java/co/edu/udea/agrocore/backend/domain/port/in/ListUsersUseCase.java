package co.edu.udea.agrocore.backend.domain.port.in;

import co.edu.udea.agrocore.backend.domain.model.User;
import java.util.List;

public interface ListUsersUseCase {
    List<User> listAll();
}
