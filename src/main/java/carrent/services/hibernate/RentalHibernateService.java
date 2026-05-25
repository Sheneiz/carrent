package carrent.services.hibernate;

import carrent.db.HibernateConfig;
import carrent.models.Rental;
import carrent.models.Vehicle;
import carrent.repositories.impl.hibernate.RentalHibernateRepository;
import carrent.repositories.impl.hibernate.UserHibernateRepository;
import carrent.repositories.impl.hibernate.VehicleHibernateRepository;
import carrent.services.inter.RentalServiceInterface;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            rentalRepo.setSession(session);
            vehicleRepo.setSession(session);

            List<String> rentedIds = rentalRepo.findAll().stream()
                    .filter(Rental::isActive)
                    .map(Rental::getVehicleId)
                    .collect(Collectors.toList());

            return vehicleRepo.findAll().stream()
                    .filter(v -> !rentedIds.contains(v.getId()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public boolean rentVehicle(String userId, String vehicleId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            rentalRepo.setSession(session);
            vehicleRepo.setSession(session);
            userRepo.setSession(session);

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
            tx.commit();
            return true;
        }
    }

    @Override
    public List<Rental> getAllActiveRentals() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            rentalRepo.setSession(session);
            return rentalRepo.findAll().stream()
                    .filter(Rental::isActive)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<Rental> getAllRentals() {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            rentalRepo.setSession(session);
            return rentalRepo.findAll();
        }
    }

    @Override
    public List<Vehicle> getRentedVehicles(String userId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            rentalRepo.setSession(session);
            vehicleRepo.setSession(session);

            List<String> userRentedIds = rentalRepo.findAll().stream()
                    .filter(r -> r.isActive() && userId.equals(r.getUserId()))
                    .map(Rental::getVehicleId)
                    .collect(Collectors.toList());

            return vehicleRepo.findAll().stream()
                    .filter(v -> userRentedIds.contains(v.getId()))
                    .collect(Collectors.toList());
        }
    }

    @Override
    public boolean returnVehicle(String vehicleId) {
        try (Session session = HibernateConfig.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            rentalRepo.setSession(session);

            boolean returned = rentalRepo.findByVehicleIdAndReturnDateIsNull(vehicleId).map(r -> {
                r.setReturnDateTime(LocalDateTime.now().toString());
                rentalRepo.save(r);
                return true;
            }).orElse(false);

            tx.commit();
            return returned;
        }
    }
}