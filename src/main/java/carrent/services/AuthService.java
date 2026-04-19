package carrent.services;

import carrent.models.Role;
import carrent.models.User;
import carrent.models.Rental;
import carrent.repositories.UserRepository;
import carrent.repositories.RentalRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepo;
    private final RentalRepository rentalRepo;

    public AuthService(UserRepository userRepo, RentalRepository rentalRepo) {
        this.userRepo = userRepo;
        this.rentalRepo = rentalRepo;
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
                    if (hash == null || hash.isEmpty()) {
                        return false;
                    }
                    try {
                        return BCrypt.checkpw(Password, hash);
                    } catch (Exception e) {
                        return false;
                    }
                });
    }

    public void deleteUser(String userId) {
        boolean hasActiveRentals = rentalRepo.findAll().stream()
                .anyMatch(rental -> rental.getUserId().equals(userId) && rental.isActive());

        if (hasActiveRentals) {
            throw new IllegalStateException("Nie można usunąć użytkownika, ponieważ ma wypożyczony pojazd!");
        }

        userRepo.deleteById(userId);
    }
}