
package carrent.services.hibernate;
import carrent.db.HibernateConfig;
import carrent.db.JsonFileStorage;
import carrent.models.Rental;
import carrent.models.Vehicle;
import carrent.repositories.impl.hibernate.RentalHibernateRepository;
import carrent.repositories.impl.hibernate.VehicleHibernateRepository;
import carrent.services.VehicleValidator;
import carrent.services.inter.VehicleServiceInterface;
import com.google.gson.reflect.TypeToken;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
public class VehicleHibernateService implements VehicleServiceInterface {
    private final VehicleHibernateRepository vehicleRepo;
    private final RentalHibernateRepository rentalRepo;
    private final JsonFileStorage<Map<String, Object>> categoryStorage;
    private final VehicleValidator validator;
    public VehicleHibernateService(VehicleHibernateRepository vehicleRepo, RentalHibernateRepository rentalRepo, String categoriesFilePath, VehicleValidator validator) {
        this.vehicleRepo = vehicleRepo;
        this.rentalRepo = rentalRepo;
        this.categoryStorage = new JsonFileStorage<>(categoriesFilePath, new TypeToken<List<Map<String, Object>>>() {}.getType());
        this.validator = validator;
    }
    private void setSession(Session session) {
        vehicleRepo.setSession(session);
        rentalRepo.setSession(session);
    }
    private void rollback(Transaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }
    @Override
    public void addVehicle(Vehicle vehicle) {
        Transaction tx = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            setSession(session);
            Map<String, Object> required = getCategoryAttributes(vehicle.getCategory());
            validator.validate(vehicle, required);
            vehicleRepo.save(vehicle);
            tx.commit();
        } catch (RuntimeException e) {
            rollback(tx);
            throw e;
        }
    }
    @Override
    public List<Vehicle> getAvailableVehicles() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            setSession(session);
            List<String> activeRentalIds = rentalRepo.findAll().stream()
                    .filter(Rental::isActive)
                    .map(Rental::getVehicleId)
                    .toList();
            return vehicleRepo.findAll().stream()
                    .filter(v -> !activeRentalIds.contains(v.getId()))
                    .toList();
        }
    }
    @Override
    public Map<String, Object> getCategoryAttributes(String categoryName) {
        List<Map<String, Object>> categories = categoryStorage.load();
        if (categories == null) return Map.of();
        return categories.stream()
                .filter(c -> categoryName.equalsIgnoreCase((String) c.get("category")))
                .findFirst()
                .map(c -> (Map<String, Object>) c.get("attributes"))
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidlowa kategoria: " + categoryName));
    }
    @Override
    public Optional<Vehicle> getVehicleById(String id) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            setSession(session);
            return vehicleRepo.findById(id);
        }
    }
    @Override
    public void deleteVehicle(String vehicleId) {
        Transaction tx = null;
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            setSession(session);
            boolean isRented = rentalRepo.findByVehicleIdAndReturnDateIsNull(vehicleId).isPresent();
            if (isRented) {
                throw new IllegalStateException("Nie mo�na usun�� pojazdu, bo jest wypo�yczony!");
            }
            vehicleRepo.deleteById(vehicleId);
            tx.commit();
        } catch (RuntimeException e) {
            rollback(tx);
            throw e;
        }
    }
}

