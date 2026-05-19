
package carrent.services.inter;
import carrent.models.Rental;
import carrent.models.Vehicle;
import java.util.List;
public interface RentalServiceInterface {
    List<Vehicle> getAvailableVehicles();
    boolean rentVehicle(String userId, String vehicleId);
    List<Rental> getAllActiveRentals();
    List<Vehicle> getRentedVehicles(String userId);
    boolean returnVehicle(String vehicleId);
}

