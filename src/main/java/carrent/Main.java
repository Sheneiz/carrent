package carrent;

import carrent.repositories.RentalRepository;
import carrent.repositories.UserRepository;
import carrent.repositories.VehicleRepository;
import carrent.repositories.impl.RentalJsonRepository;
import carrent.repositories.impl.UserJsonRepository;
import carrent.repositories.impl.VehicleJsonRepository;
import carrent.services.AuthService;
import carrent.services.RentalService;
import carrent.services.VehicleService;

public class Main {
    public static void main(String[] args) {
        String storageType = "json";

        UserRepository userRepo;
        VehicleRepository vehicleRepo;
        RentalRepository rentalRepo;

        switch(storageType) {
            case "json" -> {
                userRepo = new UserJsonRepository();
                vehicleRepo = new VehicleJsonRepository();
                rentalRepo = new RentalJsonRepository();
            }
            default -> throw new IllegalArgumentException("Unknown storage type: " + storageType);
        }

        AuthService authService = new AuthService(userRepo, rentalRepo);
        VehicleService vehicleService = new VehicleService(vehicleRepo, rentalRepo, "categories.json");
        RentalService rentalService = new RentalService(vehicleRepo,rentalRepo );
        UI app = new UI(authService, vehicleService, rentalService);
        app.run();
    }
}