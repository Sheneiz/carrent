package carrent.services;

import carrent.models.Role;
import carrent.models.User;
import carrent.repositories.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepo;

    public AuthService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public boolean register(String login, String rawPassword, String role) {
        if(userRepo.findByLogin(login).isPresent()) {
            return false;
        }

        Role parsedRole;
        try {
            parsedRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return false; 
        }

        String hashed = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        User user = User.builder()
                .login(login)
                .password(hashed)
                .role(parsedRole)
                .build();

        userRepo.save(user);
        return true;
    }

    public Optional<User> login(String login, String Password) {
        return userRepo.findByLogin(login)
                .filter(user -> {
                    String hash = user.getPassword();
                    if (hash == null || !hash.startsWith("$2a$")) {
                        return false;
                    }
                    try {
                        return BCrypt.checkpw(Password, hash);
                    } catch (Exception e) {
                        return false;
                    }
                });
    }
}