package carrent.services.hibernate;

import carrent.db.HibernateConfig;
import carrent.models.Rental;
import carrent.models.Vehicle;
import carrent.repositories.impl.hibernate.RentalHibernateRepository;
import carrent.repositories.impl.hibernate.UserHibernateRepository;
import carrent.repositories.impl.hibernate.VehicleHibernateRepository;
import carrent.services.inter.RentalServiceInterface;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RentalHibernateService implements RentalServiceInterface {
    private final RentalHibernateRepository rentalRepo;
    private final VehicleHibernateRepository vehicleRepo;
    private final UserHibernateRepository userRepo;

    public RentalHibernateService(RentalHibernateRepository rentalRepo, VehicleHibernateRepository vehicleRepo, UserHibernateRepository userRepo) {
        this.rentalRepo = rentalRepo;
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
    }

    @Override
    public List<Vehicle> getAvailableVehicles() {
        List<String> rentedIds = rentalRepo.findAll().stream()
                .filter(Rental::isActive)
                .map(Rental::getVehicleId)
                .collect(Collectors.toList());

        return vehicleRepo.findAll().stream()
                .filter(v -> !rentedIds.contains(v.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean rentVehicle(String userId, String vehicleId) {
        boolean isBusy = rentalRepo.findAll().stream()
                .filter(Rental::isActive)
                .anyMatch(r -> r.getVehicleId().equals(vehicleId));
        if (isBusy) return false;

        var vehicle = vehicleRepo.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono pojazdu"));
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika"));

        Rental rental = Rental.builder()
                .id(java.util.UUID.randomUUID().toString())
                .user(user)
                .vehicle(vehicle)
                .rentDateTime(LocalDateTime.now().toString())
                .build();

        rentalRepo.save(rental);
        return true;
    }

    @Override
    public List<Rental> getAllActiveRentals() {
        return rentalRepo.findAll().stream()
                .filter(Rental::isActive)
                .collect(Collectors.toList());
    }

    @Override
    public List<Rental> getAllRentals() {
        return rentalRepo.findAll();
    }

    @Override
    public List<Vehicle> getRentedVehicles(String userId) {
        List<String> userRentedIds = rentalRepo.findAll().stream()
                .filter(r -> r.isActive() && userId.equals(r.getUserId()))
                .map(Rental::getVehicleId)
                .collect(Collectors.toList());

        return vehicleRepo.findAll().stream()
                .filter(v -> userRentedIds.contains(v.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean returnVehicle(String vehicleId) {
        return rentalRepo.findByVehicleIdAndReturnDateIsNull(vehicleId).map(r -> {
            r.setReturnDateTime(LocalDateTime.now().toString());
            rentalRepo.save(r);
            return true;
        }).orElse(false);
    }
}