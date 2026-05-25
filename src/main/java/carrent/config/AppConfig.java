package carrent.config;

import carrent.repositories.impl.hibernate.RentalHibernateRepository;
import carrent.repositories.impl.hibernate.UserHibernateRepository;
import carrent.repositories.impl.hibernate.VehicleHibernateRepository;
import carrent.services.VehicleValidator;
import carrent.services.hibernate.AuthHibernateService;
import carrent.services.hibernate.RentalHibernateService;
import carrent.services.hibernate.VehicleHibernateService;
import carrent.services.inter.AuthServiceInterface;
import carrent.services.inter.RentalServiceInterface;
import carrent.services.inter.VehicleServiceInterface;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("hibernate")
public class AppConfig {

    @Bean
    public UserHibernateRepository userRepo() {
        return new UserHibernateRepository();
    }

    @Bean
    public VehicleHibernateRepository vehicleRepo() {
        return new VehicleHibernateRepository();
    }

    @Bean
    public RentalHibernateRepository rentalRepo() {
        return new RentalHibernateRepository();
    }

    @Bean
    public VehicleValidator vehicleValidator() {
        return new VehicleValidator();
    }

    @Bean
    public AuthServiceInterface authService() {
        return new AuthHibernateService(userRepo(), rentalRepo());
    }

    @Bean
    public VehicleServiceInterface vehicleService() {
        return new VehicleHibernateService(vehicleRepo(), rentalRepo(), "categories.json", vehicleValidator());
    }

    @Bean
    public RentalServiceInterface rentalService() {
        return new RentalHibernateService(rentalRepo(), vehicleRepo(), userRepo());
    }
}