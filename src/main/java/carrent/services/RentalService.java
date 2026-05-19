package carrent.services;

import carrent.models.*;
import carrent.repositories.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class RentalService {
    private final VehicleRepository vehicleRepo;
    private final RentalRepository rentalRepo;

    public RentalService(VehicleRepository vehicleRepo, RentalRepository rentalRepo) {
        this.vehicleRepo = vehicleRepo;
        this.rentalRepo = rentalRepo;
    }

    public List<Vehicle> getAvailableVehicles() {
        List<String> rentedIds = rentalRepo.findAll().stream()
                .filter(Rental::isActive)
                .map(r -> r.getVehicle().getId())
                .collect(Collectors.toList());

        return vehicleRepo.findAll().stream()
                .filter(v -> !rentedIds.contains(v.getId()))
                .collect(Collectors.toList());
    }

    public boolean rentVehicle(String userId, String vehicleId) {
      boolean isBusy = rentalRepo.findAll().stream()
                .filter(Rental::isActive)
                .anyMatch(r -> r.getVehicle().getId().equals(vehicleId));
        if (isBusy) return false;
        Vehicle vehicle = vehicleRepo.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono pojazdu"));

        User user = User.builder().id(userId).build();
        Rental rental = Rental.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .vehicle(vehicle)
                .rentDateTime(LocalDateTime.now().toString())
                .build();

        rentalRepo.save(rental);
        return true;
    }

    public List<Rental> getAllActiveRentals() {
        return rentalRepo.findAll().stream()
                .filter(Rental::isActive)
                .collect(Collectors.toList());
    }

    public List<Vehicle> getRentedVehicles(String userId) {
        List<String> userRentedIds = rentalRepo.findAll().stream()
                .filter(r -> r.isActive() && userId.equals(r.getUser().getId()))
                .map(r -> r.getVehicle().getId())
                .collect(Collectors.toList());

        return vehicleRepo.findAll().stream()
                .filter(v -> userRentedIds.contains(v.getId()))
                .collect(Collectors.toList());
    }

    public boolean returnVehicle(String vehicleId) {
        Optional<Rental> activeRental = rentalRepo.findAll().stream()
                .filter(Rental::isActive)
                .filter(r -> r.getVehicle().getId().equals(vehicleId))
                .findFirst();

        return activeRental.map(r -> {
            r.setReturnDateTime(LocalDateTime.now().toString());
            rentalRepo.save(r);
            return true;
        }).orElse(false);
    }
}
