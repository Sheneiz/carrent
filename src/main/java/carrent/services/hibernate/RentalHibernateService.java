
package carrent.services.hibernate;
import carrent.db.HibernateConfig;
import carrent.models.Rental;
import carrent.models.User;
import carrent.models.Vehicle;
import carrent.repositories.impl.hibernate.RentalHibernateRepository;
import carrent.repositories.impl.hibernate.UserHibernateRepository;
import carrent.repositories.impl.hibernate.VehicleHibernateRepository;
import carrent.services.inter.RentalServiceInterface;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
public class RentalHibernateService implements RentalServiceInterface {
    private final RentalHibernateRepository rentalRepo;
    private final VehicleHibernateRepository vehicleRepo;
    private final UserHibernateRepository userRepo;
    public RentalHibernateService(RentalHibernateRepository rentalRepo, VehicleHibernateRepository vehicleRepo, UserHibernateRepository userRepo) {
        this.rentalRepo = rentalRepo;
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
    }
    private void setSession(Session session) {
        rentalRepo.setSession(session);
        vehicleRepo.setSession(session);
        userRepo.setSession(session);
    }
    private void rollback(Transaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }
    @Override
    public List<Vehicle> getAvailableVehicles() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            setSession(session);
            List<String> rentedIds = rentalRepo.findAll().stream()
                    .filter(Rental::isActive)
                    .map(Rental::getVehicleId)
                    .collect(Collectors.toList());
            return vehicleRepo.findAll().stream()
                    .filter(v -> !rentedIds.contains(v.getId()))
                    .collect(Collectors.toList());
        }
    }
    @Override
    public boolean rentVehicle(String userId, String vehicleId) {
        Transaction tx = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            setSession(session);
            boolean isBusy = rentalRepo.findByVehicleIdAndReturnDateIsNull(vehicleId).isPresent();
            if (isBusy) {
                return false;
            }
            Vehicle vehicle = vehicleRepo.findById(vehicleId).orElse(null);
            User user = userRepo.findById(userId).orElse(null);
            if(vehicle == null || user == null) return false;
            Rental rental = Rental.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .vehicle(vehicle)
                    .rentDateTime(LocalDateTime.now().toString())
                    .build();
            rentalRepo.save(rental);
            tx.commit();
            return true;
        } catch (RuntimeException e) {
            rollback(tx);
            throw e;
        }
    }
    @Override
    public List<Rental> getAllActiveRentals() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            setSession(session);
            return rentalRepo.findAll().stream()
                    .filter(Rental::isActive)
                    .collect(Collectors.toList());
        }
    }
    @Override
    public List<Vehicle> getRentedVehicles(String userId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            setSession(session);
            List<String> userRentedIds = rentalRepo.findAll().stream()
                    .filter(r -> r.isActive() && r.getUser() != null && userId.equals(r.getUser().getId()))
                    .map(Rental::getVehicleId)
                    .collect(Collectors.toList());
            return vehicleRepo.findAll().stream()
                    .filter(v -> userRentedIds.contains(v.getId()))
                    .collect(Collectors.toList());
        }
    }
    @Override
    public boolean returnVehicle(String vehicleId) {
        Transaction tx = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            setSession(session);
            boolean returned = rentalRepo.findByVehicleIdAndReturnDateIsNull(vehicleId).map(r -> {
                r.setReturnDateTime(LocalDateTime.now().toString());
                rentalRepo.save(r);
                return true;
            }).orElse(false);
            tx.commit();
            return returned;
        } catch (RuntimeException e) {
            rollback(tx);
            throw e;
        }
    }
}

