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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class VehicleHibernateService implements VehicleServiceInterface {
    private final VehicleHibernateRepository vehicleRepo;
    private final RentalHibernateRepository rentalRepo;
    private final JsonFileStorage<Map<String, Object>> categoryStorage;
    private final VehicleValidator validator;

    public VehicleHibernateService(
            VehicleHibernateRepository vehicleRepo, 
            RentalHibernateRepository rentalRepo, 
            @Value("${app.categories.path:categories.json}") String categoriesFilePath, 
            VehicleValidator validator) {
        this.vehicleRepo = vehicleRepo;
        this.rentalRepo = rentalRepo;
        this.categoryStorage = new JsonFileStorage<>(categoriesFilePath, new TypeToken<List<Map<String, Object>>>() {}.getType());
        this.validator = validator;
    }

    @Override
    public void addVehicle(Vehicle vehicle) {
        Map<String, Object> required = getCategoryAttributes(vehicle.getCategory());
        validator.validate(vehicle, required);

        vehicleRepo.save(vehicle);
    }

    @Override
    public List<Vehicle> getAvailableVehicles() {
        List<String> activeRentalIds = rentalRepo.findAll().stream()
                .filter(Rental::isActive)
                .map(Rental::getVehicleId)
                .toList();

        return vehicleRepo.findAll().stream()
                .filter(v -> !activeRentalIds.contains(v.getId()))
                .toList();
    }

    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepo.findAll();
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
        return vehicleRepo.findById(id);
    }

    @Override
    public void deleteVehicle(String vehicleId) {
        boolean isRented = rentalRepo.findByVehicleIdAndReturnDateIsNull(vehicleId).isPresent();
        if (isRented) {
            throw new IllegalStateException("Nie można usunąć pojazdu, bo jest wypożyczony!");
        }

        vehicleRepo.deleteById(vehicleId);
    }
}