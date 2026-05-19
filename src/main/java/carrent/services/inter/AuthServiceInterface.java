
package carrent.services.inter;
import carrent.models.User;
import java.util.List;
import java.util.Optional;
public interface AuthServiceInterface {
    boolean register(String login, String rawPassword, String role);
    Optional<User> login(String login, String password);
    List<User> getAllUsers();
    Optional<User> getUserById(String id);
    void deleteUser(String userId);
}

