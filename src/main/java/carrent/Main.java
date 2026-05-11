
package carrent;
import carrent.repositories.impl.hibernate.RentalHibernateRepository;
import carrent.repositories.impl.hibernate.UserHibernateRepository;
import carrent.repositories.impl.hibernate.VehicleHibernateRepository;
import carrent.services.*;
import carrent.services.hibernate.AuthHibernateService;
import carrent.services.hibernate.RentalHibernateService;
import carrent.services.hibernate.VehicleHibernateService;
import carrent.services.inter.AuthServiceInterface;
import carrent.services.inter.RentalServiceInterface;
import carrent.services.inter.VehicleServiceInterface;

public class Main {
    public static void main(String[] args) {
        System.out.println("Zainicjowano z pamiecia bazodanowa (Hibernate).");
        UserHibernateRepository userRepo = new UserHibernateRepository();
        VehicleHibernateRepository vehicleRepo = new VehicleHibernateRepository();
        RentalHibernateRepository rentalRepo = new RentalHibernateRepository();
        VehicleValidator vehicleValidator = new VehicleValidator();
        AuthServiceInterface authService = new AuthHibernateService(userRepo, rentalRepo);
        VehicleServiceInterface vehicleService = new VehicleHibernateService(
                vehicleRepo,
                rentalRepo,
                "categories.json",
                vehicleValidator
        );
        RentalServiceInterface rentalService = new RentalHibernateService(rentalRepo, vehicleRepo, userRepo);
        UI app = new UI(authService, vehicleService, rentalService);
        app.run();
    }
}

