
package carrent.services.inter;
import carrent.models.Vehicle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
public interface VehicleServiceInterface {
    void addVehicle(Vehicle vehicle);
    List<Vehicle> getAvailableVehicles();
    Map<String, Object> getCategoryAttributes(String categoryName);
    Optional<Vehicle> getVehicleById(String id);
    void deleteVehicle(String vehicleId);
}

