package carrent.services.hibernate;

import carrent.db.HibernateConfig;
import carrent.models.Role;
import carrent.models.User;
import carrent.repositories.impl.hibernate.RentalHibernateRepository;
import carrent.repositories.impl.hibernate.UserHibernateRepository;
import carrent.services.inter.AuthServiceInterface;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthHibernateService implements AuthServiceInterface {
    private final UserHibernateRepository userRepo;
    private final RentalHibernateRepository rentalRepo;

    public AuthHibernateService(UserHibernateRepository userRepo, RentalHibernateRepository rentalRepo) {
        this.userRepo = userRepo;
        this.rentalRepo = rentalRepo;
    }

    @Override
    public Optional<User> login(String login, String password) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            userRepo.setSession(session);
            return userRepo.findByLogin(login)
                    .filter(user -> BCrypt.checkpw(password, user.getPassword()));
        }
    }

    @Override
    public boolean register(String login, String rawPassword, String role) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            userRepo.setSession(session);

            if (userRepo.findByLogin(login).isPresent()) {
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
                    .id(UUID.randomUUID().toString())
                    .login(login)
                    .password(hashed)
                    .role(parsedRole)
                    .build();

            userRepo.save(user);
            tx.commit();
            return true;
        }
    }

    @Override
    public List<User> getAllUsers() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            userRepo.setSession(session);
            return userRepo.findAll();
        }
    }

    @Override
    public Optional<User> getUserById(String id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            userRepo.setSession(session);
            return userRepo.findById(id);
        }
    }

    @Override
    public void deleteUser(String userId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            userRepo.setSession(session);
            rentalRepo.setSession(session);

            boolean hasActiveRentals = rentalRepo.findAll().stream()
                    .anyMatch(rental -> rental.getUser().getId().equals(userId) && rental.isActive());

            if (hasActiveRentals) {
                throw new IllegalStateException("Nie można usunąć użytkownika, ponieważ ma wypożyczony pojazd!");
            }

            userRepo.deleteById(userId);
            tx.commit();
        }
    }
}