package carrent;

import carrent.repositories.RentalRepository;
import carrent.repositories.UserRepository;
import carrent.repositories.VehicleRepository;
import carrent.repositories.impl.RentalJsonRepository;
import carrent.repositories.impl.UserJsonRepository;
import carrent.repositories.impl.VehicleJsonRepository;
import carrent.repositories.impl.RentalJdbcRepository;
import carrent.repositories.impl.UserJdbcRepository;
import carrent.repositories.impl.VehicleJdbcRepository;
import carrent.services.AuthService;
import carrent.services.RentalService;
import carrent.services.VehicleService;
import carrent.services.VehicleValidator;

public class Main {
    public static void main(String[] args) {
        String storageType = args.length > 0 ? args[0].toLowerCase() : "jdbc";

        UserRepository userRepo;
        VehicleRepository vehicleRepo;
        RentalRepository rentalRepo;

        switch(storageType) {
            case "jdbc" -> {
                System.out.println("Zainicjowano z pamięcią bazodanową (JDBC).");
                userRepo = new UserJdbcRepository();
                vehicleRepo = new VehicleJdbcRepository();
                rentalRepo = new RentalJdbcRepository();
            }
            case "json" -> {
                System.out.println("Zainicjowano z pamięcią lokalną plików (.json).");
                userRepo = new UserJsonRepository();
                vehicleRepo = new VehicleJsonRepository();
                rentalRepo = new RentalJsonRepository();
            }
            default -> throw new IllegalArgumentException("Unknown storage type: " + storageType);
        }
        VehicleValidator vehicleValidator = new VehicleValidator();
        AuthService authService = new AuthService(userRepo, rentalRepo);
        VehicleService vehicleService = new VehicleService(
                vehicleRepo,
                rentalRepo,
                "categories.json",
                vehicleValidator
        );        RentalService rentalService = new RentalService(vehicleRepo,rentalRepo );
        UI app = new UI(authService, vehicleService, rentalService);
        app.run();
    }
}