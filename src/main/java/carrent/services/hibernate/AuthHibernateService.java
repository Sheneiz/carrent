
package carrent.services.hibernate;
import carrent.db.HibernateConfig;
import carrent.models.Role;
import carrent.models.User;
import carrent.repositories.impl.hibernate.UserHibernateRepository;
import carrent.repositories.impl.hibernate.RentalHibernateRepository;
import carrent.services.inter.AuthServiceInterface;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;
import java.util.List;
import java.util.Optional;
public class AuthHibernateService implements AuthServiceInterface {
    private final UserHibernateRepository userRepo;
    private final RentalHibernateRepository rentalRepo;
    public AuthHibernateService(UserHibernateRepository userRepo, RentalHibernateRepository rentalRepo) {
        this.userRepo = userRepo;
        this.rentalRepo = rentalRepo;
    }
    private void setSession(Session session) {
        userRepo.setSession(session);
        rentalRepo.setSession(session);
    }
    private void rollback(Transaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }
    @Override
    public boolean register(String login, String rawPassword, String role) {
        Transaction tx = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            setSession(session);
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
                    .id(java.util.UUID.randomUUID().toString())
                    .login(login)
                    .password(hashed)
                    .role(parsedRole)
                    .build();
            userRepo.save(user);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            throw e;
        }
    }
    @Override
    public Optional<User> login(String login, String password) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            setSession(session);
            return userRepo.findByLogin(login)
                    .filter(user -> {
                        String hash = user.getPassword();
                        if (hash == null || hash.isEmpty()) return false;
                        try {
                            return BCrypt.checkpw(password, hash);
                        } catch (Exception e) {
                            return false;
                        }
                    });
        }
    }
    @Override
    public List<User> getAllUsers() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            setSession(session);
            return userRepo.findAll();
        }
    }
    @Override
    public Optional<User> getUserById(String id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            setSession(session);
            return userRepo.findById(id);
        }
    }
    @Override
    public void deleteUser(String userId) {
        Transaction tx = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            setSession(session);
            boolean hasActiveRentals = rentalRepo.findAll().stream()
                    .anyMatch(rental -> rental.getUser().getId().equals(userId) && rental.isActive());
            if (hasActiveRentals) {
                throw new IllegalStateException("Nie mo�na usun�� u�ytkownika, poniewa� ma wypo�yczony pojazd!");
            }
            userRepo.deleteById(userId);
            tx.commit();
        } catch (RuntimeException e) {
            rollback(tx);
            throw e;
        }
    }
}

