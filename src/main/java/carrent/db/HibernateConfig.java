package carrent.db;

import carrent.models.Rental;
import carrent.models.User;
import carrent.models.Vehicle;
import lombok.Getter;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
public class HibernateConfig {
    @Getter
    private static final SessionFactory sessionFactory;
    static {
        try {
            Configuration configuration = new Configuration();
            configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            String dbUrl = System.getenv("DB_URL");
            
            // Jeśli nie przypisano DB_URL, powiadom użytkownika
            if (dbUrl == null || dbUrl.isEmpty()) {
                throw new IllegalArgumentException("Zmienna srodowiskowa DB_URL nie jest ustawiona! Dodaj DB_URL=jdbc:... w konfiguracji uruchamiania IDE.");
            }
            
            configuration.setProperty("hibernate.connection.url", dbUrl);
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            configuration.setProperty("hibernate.show_sql", "false");
            configuration.setProperty("hibernate.format_sql", "false");
            configuration.setProperty("hibernate.hbm2ddl.auto", "update");
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Vehicle.class);
            configuration.addAnnotatedClass(Rental.class);
            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError("Initialize Hibernate ERROR: " + ex);
        }
    }
    private HibernateConfig() {}
}
