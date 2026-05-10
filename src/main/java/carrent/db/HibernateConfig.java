package carrent.db;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

import java.util.HashMap;
import java.util.Map;

public final class HibernateConfig {
    private static SessionFactory sessionFactory;

    private HibernateConfig() {
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = buildSessionFactory();
        }
        return sessionFactory;
    }

    private static SessionFactory buildSessionFactory() {
        String url = getValue("DB_URL", "db.url");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("DB_URL not set. Set environment variable DB_URL or VM option -Ddb.url=jdbc:...");
        }

        Map<String, Object> settings = new HashMap<>();
        settings.put(AvailableSettings.DRIVER, "org.postgresql.Driver");
        settings.put(AvailableSettings.URL, url);
        settings.put(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect");
        settings.put(AvailableSettings.HBM2DDL_AUTO, "update");
        settings.put(AvailableSettings.SHOW_SQL, false);
        settings.put(AvailableSettings.FORMAT_SQL, true);

        String username = getValue("DB_USER", "db.user");
        if (username != null && !username.isBlank()) {
            settings.put(AvailableSettings.USER, username);
        }

        String password = getValue("DB_PASSWORD", "db.password");
        if (password != null && !password.isBlank()) {
            settings.put(AvailableSettings.PASS, password);
        }

        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(settings)
                .build();

        try {
            return new MetadataSources(registry)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception e) {
            StandardServiceRegistryBuilder.destroy(registry);
            throw e;
        }
    }

    public static synchronized void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }

    private static String getValue(String envName, String propertyName) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return System.getProperty(propertyName);
    }
}
