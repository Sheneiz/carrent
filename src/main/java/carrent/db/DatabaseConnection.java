package carrent.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private final String url;

    private DatabaseConnection() {
        String env = System.getenv("DB_URL");
        String prop = System.getProperty("db.url");
        this.url = (env != null && !env.isBlank()) ? env : prop;

        if (this.url == null || this.url.isBlank()) {
            throw new RuntimeException("DB_URL not set. Set environment variable DB_URL or VM option -Ddb.url=jdbc:...");
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) instance = new DatabaseConnection();
        return instance;
    }

    public static Connection getConnection() {
        return getInstance().createConnection();
    }

    private Connection createConnection() {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new RuntimeException("Connection Failed!", e);
        }
    }
}
