package carrent.services;

import carrent.models.Vehicle;
import carrent.models.Rental;
import carrent.repositories.VehicleRepository;
import carrent.repositories.RentalRepository;
import carrent.db.JsonFileStorage;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class VehicleService {
    private final VehicleRepository vehicleRepo;
    private final RentalRepository rentalRepo;
    private final JsonFileStorage<Map<String, Object>> categoryStorage;
    private final VehicleValidator validator;

    public VehicleService(VehicleRepository vehicleRepo, RentalRepository rentalRepo, String categoriesFilePath, VehicleValidator validator) {
        this.vehicleRepo = vehicleRepo;
        this.rentalRepo = rentalRepo;
        this.categoryStorage = new JsonFileStorage<>(categoriesFilePath, new TypeToken<List<Map<String, Object>>>() {}.getType());
        this.validator = validator; // Teraz poprawnie przypisujemy wstrzyknięty obiekt
    }

    public void addVehicle(Vehicle vehicle) {
        Map<String, String> required = getCategoryAttributes(vehicle.getCategory());
        validator.validate(vehicle, required);
        vehicleRepo.save(vehicle);
    }

    public List<Vehicle> getAvailableVehicles() {
        List<String> activeRentalIds = rentalRepo.findAll().stream()
                .filter(Rental::isActive)
                .map(Rental::getVehicleId)
                .toList();

        return vehicleRepo.findAll().stream()
                .filter(v -> !activeRentalIds.contains(v.getId()))
                .toList();
    }

    public Map<String, String> getCategoryAttributes(String categoryName) {
        List<Map<String, Object>> categories = categoryStorage.load();
        if (categories == null) return Map.of();

        return categories.stream()
                .filter(c -> categoryName.equalsIgnoreCase((String) c.get("category")))
                .findFirst()
                .map(c -> (Map<String, String>) c.get("attributes"))
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowa kategoria: " + categoryName));
    }

    public Optional<Vehicle> getVehicleById(String id) {
        return vehicleRepo.findById(id);
    }

    public void deleteVehicle(String vehicleId) {
        boolean isRented = rentalRepo.findByVehicleIdAndReturnDateIsNull(vehicleId).isPresent();
        if (isRented) {
            throw new IllegalStateException("Nie można usunąć pojazdu, bo jest wypożyczony!");
        }
        vehicleRepo.deleteById(vehicleId);
    }
}