package carrent.services.hibernate;

import carrent.models.Role;
import carrent.models.User;
import carrent.repositories.impl.hibernate.RentalHibernateRepository;
import carrent.repositories.impl.hibernate.UserHibernateRepository;
import carrent.services.inter.AuthServiceInterface;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile("hibernate") // <- DODANE: Spring wie, że ma użyć tego serwisu dla profilu Hibernate
@Transactional
public class AuthHibernateService implements AuthServiceInterface {
    private final UserHibernateRepository userRepo;
    private final RentalHibernateRepository rentalRepo;

    public AuthHibernateService(UserHibernateRepository userRepo, RentalHibernateRepository rentalRepo) {
        this.userRepo = userRepo;
        this.rentalRepo = rentalRepo;
    }

    @Override
    public Optional<User> login(String login, String password) {
        return userRepo.findByLogin(login)
                .filter(user -> BCrypt.checkpw(password, user.getPassword()));
    }

    @Override
    public boolean register(String login, String rawPassword, String role) {
        if (userRepo.findByLogin(login).isPresent()) {
            return false;
        }

        Role parsedRole;
        try {
            parsedRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return false;
        }

        String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        User user = User.builder()
                .id(java.util.UUID.randomUUID().toString())
                .login(login)
                .password(hashedPassword)
                .role(parsedRole)
                .build();

        userRepo.save(user);
        return true;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public Optional<User> getUserById(String id) {
        return userRepo.findById(id);
    }

    @Override
    public void deleteUser(String userId) {
        boolean hasActiveRentals = rentalRepo.findAll().stream()
                .anyMatch(rental -> rental.getUser().getId().equals(userId) && rental.isActive());

        if (hasActiveRentals) {
            throw new IllegalStateException("Nie można usunąć użytkownika, ponieważ ma wypożyczony pojazd!");
        }

        userRepo.deleteById(userId);
    }
}