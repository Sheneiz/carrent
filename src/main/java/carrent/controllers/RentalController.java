package carrent.controllers;

import carrent.models.Rental;
import carrent.services.RentalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final carrent.services.inter.RentalServiceInterface rentalService;

    public RentalController(carrent.services.inter.RentalServiceInterface rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public List<Rental> list() {
        return rentalService.getAllRentals();
    }

    @GetMapping("/users/{userId}")
    public List<Rental> userRentals(@PathVariable String userId) {
        return rentalService.getAllRentals().stream()
                .filter(r -> r.getUser() != null && userId.equals(r.getUser().getId()))
                .toList();
    }

    @PostMapping("/users/{userId}/rent/{vehicleId}")
    public Rental rent(@PathVariable String userId, @PathVariable String vehicleId) {
        boolean success = rentalService.rentVehicle(userId, vehicleId);
        if(!success) {
             throw new IllegalStateException("Nie można wypożyczyć pojazdu.");
        }
        return rentalService.getAllRentals().stream()
                .filter(r -> r.getVehicleId().equals(vehicleId) && r.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Błąd"));
    }

    @PostMapping("/users/{userId}/return")
    public Rental returnVehicle(@PathVariable String userId) {
        Rental activeRental = rentalService.getAllRentals().stream()
                .filter(r -> r.getUser() != null && userId.equals(r.getUser().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ten użytkownik nie ma aktualnie żadnego wypożyczonego pojazdu."));

        boolean success = rentalService.returnVehicle(activeRental.getVehicleId());
        if (!success) {
            throw new IllegalStateException("Zwrot pojazdu się nie powiódł.");
        }
        
        // This is a bit of a hack since returnVehicle doesn't return the rental, but we just return what we fetched
        activeRental.setReturnDateTime(java.time.LocalDateTime.now().toString());
        return activeRental;
    }
}