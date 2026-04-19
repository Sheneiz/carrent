package carrent.services;

import carrent.models.Vehicle;
import carrent.models.Rental;
import carrent.repositories.VehicleRepository;
import carrent.repositories.RentalRepository;
import carrent.db.JsonFileStorage;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VehicleService {
    private final VehicleRepository vehicleRepo;
    private final RentalRepository rentalRepo;
    private final JsonFileStorage<Map<String, Object>> categoryStorage;

    public VehicleService(VehicleRepository vehicleRepo, RentalRepository rentalRepo, String categoriesFilePath) {
        this.vehicleRepo = vehicleRepo;
        this.rentalRepo = rentalRepo;
        this.categoryStorage = new JsonFileStorage<>(categoriesFilePath, new TypeToken<List<Map<String, Object>>>() {}.getType());
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
        return categories.stream()
                .filter(c -> categoryName.equalsIgnoreCase((String) c.get("category")))
                .findFirst()
                .map(c -> (Map<String, String>) c.get("attributes"))
                .orElseThrow(() -> new IllegalArgumentException("Nieprawidłowa lub nieobsługiwana kategoria: " + categoryName));
    }
    
    public void addVehicle(Vehicle vehicle) {
        validate(vehicle);
        if (vehicle.getId() == null || vehicle.getId().isBlank()) {
            vehicle.setId(UUID.randomUUID().toString());
        }
        vehicleRepo.save(vehicle);
    }
    
    private void validate(Vehicle vehicle) {
        if (vehicle.getBrand() == null || vehicle.getBrand().isBlank()) {
            throw new IllegalArgumentException("Brand is required.");
        }
        if (vehicle.getModel() == null || vehicle.getModel().isBlank()) {
            throw new IllegalArgumentException("Model is required.");
        }
        if (vehicle.getPlate() == null || vehicle.getPlate().isBlank()) {
            throw new IllegalArgumentException("Plate is required.");
        }
        if (vehicle.getYear() <= 1900) {
            throw new IllegalArgumentException("Invalid year.");
        }
        if (vehicle.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }
        if (vehicle.getCategory() == null || vehicle.getCategory().isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }

        Map<String, String> expectedAttributes = getCategoryAttributes(vehicle.getCategory());
        Map<String, Object> providedAttributes = vehicle.getAttributes();

        if (expectedAttributes != null) {
            for (Map.Entry<String, String> entry : expectedAttributes.entrySet()) {
                String expectedKey = entry.getKey();
                String expectedType = String.valueOf(entry.getValue()).toUpperCase();

                String actualKey = providedAttributes.keySet().stream()
                        .filter(k -> k.equalsIgnoreCase(expectedKey))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Missing required attribute: " + expectedKey));

                Object value = providedAttributes.get(actualKey);
                if (!isTypeValid(value, expectedType)) {
                    throw new IllegalArgumentException("Invalid type for attribute: " + expectedKey + ". Expected: " + expectedType);
                }
            }
        }
    }

    private boolean isTypeValid(Object value, String expectedType) {
        if (value == null) return false;
        String valStr = value.toString();
        switch (expectedType) {
            case "STRING":
                return true;
            case "INT":
            case "INTEGER":
                try { Integer.parseInt(valStr); return true; } catch (NumberFormatException e) { return false; }
            case "DOUBLE":
                try { Double.parseDouble(valStr); return true; } catch (NumberFormatException e) { return false; }
            case "BOOLEAN":
                return valStr.equalsIgnoreCase("true") || valStr.equalsIgnoreCase("false");
            default:
                return false;
        }
    }

    public void deleteVehicle(String vehicleId) {
        boolean isRented = rentalRepo.findByVehicleIdAndReturnDateIsNull(vehicleId).isPresent();
        if (isRented) {
            throw new IllegalStateException("Nie można usunąć pojazdu, bo jest wypożyczony!");
        }
        vehicleRepo.deleteById(vehicleId);
    }
}