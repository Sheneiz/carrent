package carrent.controllers;

import carrent.models.Rental;
import carrent.models.User;
import carrent.repositories.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final carrent.services.inter.RentalServiceInterface rentalService;
    private final UserRepository userRepository;

    public RentalController(carrent.services.inter.RentalServiceInterface rentalService, UserRepository userRepository) {
        this.rentalService = rentalService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Rental> list() {
        return rentalService.getAllRentals();
    }

    @GetMapping("/users/{userId}")
    public List<Rental> userRentals(@PathVariable String userId, Principal principal) {
        validateUserAccess(userId, principal);

        return rentalService.getAllRentals().stream()
                .filter(r -> r.getUser() != null && userId.equals(r.getUser().getId()))
                .toList();
    }

    @PostMapping("/users/{userId}/rent/{vehicleId}")
    public Rental rent(@PathVariable String userId, @PathVariable String vehicleId, Principal principal) {
        validateUserAccess(userId, principal);

        boolean success = rentalService.rentVehicle(userId, vehicleId);
        if(!success) {
            throw new IllegalStateException("Nie można wypożyczyć pojazdu.");
        }

        return rentalService.getAllRentals().stream()
                .filter(r -> r.getVehicleId().equals(vehicleId) && r.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Błąd podczas pobierania nowego wypożyczenia"));
    }

    @PostMapping("/users/{userId}/return")
    public Rental returnVehicle(@PathVariable String userId, Principal principal) {
        validateUserAccess(userId, principal);

        Rental activeRental = rentalService.getAllRentals().stream()
                .filter(r -> r.getUser() != null && userId.equals(r.getUser().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ten użytkownik nie ma aktualnie żadnego wypożyczonego pojazdu."));

        boolean success = rentalService.returnVehicle(activeRental.getVehicleId());
        if (!success) {
            throw new IllegalStateException("Zwrot pojazdu się nie powiódł.");
        }

        activeRental.setReturnDateTime(java.time.LocalDateTime.now().toString());
        return activeRental;
    }

    private void validateUserAccess(String userId, Principal principal) {
        String loggedInLogin = principal.getName();
        User loggedInUser = userRepository.findByLogin(loggedInLogin)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zalogowanego użytkownika."));

        if (!"ADMIN".equals(loggedInUser.getRole().name()) && !loggedInUser.getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Brak uprawnień do wykonania operacji na koncie innego użytkownika!");
        }
    }
}