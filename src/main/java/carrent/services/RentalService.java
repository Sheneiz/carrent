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
                .map(Rental::getVehicleId)
                .collect(Collectors.toList());

        return vehicleRepo.findAll().stream()
                .filter(v -> !rentedIds.contains(v.getId()))
                .collect(Collectors.toList());
    }

    public boolean rentVehicle(String userId, String vehicleId) {
        boolean isBusy = rentalRepo.findByVehicleIdAndReturnDateIsNull(vehicleId).isPresent();
        if (isBusy) return false;

        Rental rental = Rental.builder()
                .userId(userId)
                .vehicleId(vehicleId)
                .rentDateTime(LocalDateTime.now().toString())
                .build();
        rentalRepo.save(rental);
        return true;
    }

    public boolean returnVehicle(String vehicleId) {
        return rentalRepo.findByVehicleIdAndReturnDateIsNull(vehicleId).map(r -> {
            r.setReturnDateTime(LocalDateTime.now().toString());
            rentalRepo.save(r);
            return true;
        }).orElse(false);
    }
}