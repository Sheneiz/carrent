package carrent.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection getConnection() throws SQLException {
        String dbUrl = "jdbc:postgresql://ep-red-surf-amzkrdd2-pooler.c-5.us-east-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_fY6QqhRV4BMP&sslmode=require&channel_binding=require";
        
        return DriverManager.getConnection(dbUrl);
    }
}
