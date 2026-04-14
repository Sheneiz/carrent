package carrent.services;

import carrent.models.Vehicle;
import carrent.models.Rental;
import carrent.repositories.VehicleRepository;
import carrent.repositories.RentalRepository;
import java.util.List;

public class VehicleService {
    private final VehicleRepository vehicleRepo;
    private final RentalRepository rentalRepo;

    public VehicleService(VehicleRepository vehicleRepo, RentalRepository rentalRepo) {
        this.vehicleRepo = vehicleRepo;
        this.rentalRepo = rentalRepo;
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
}