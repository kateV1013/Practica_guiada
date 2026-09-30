package ni.edu.uam.practica.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Se cambió "registro_pae" por "tienda_javafx"
    private static final String URL = readConfig("DB_URL", "db.url", "jdbc:postgresql://localhost:5432/tienda_javafx");
    private static final String USER = readConfig("DB_USER", "db.user", "postgres");
    private static final String PASSWORD = readConfig("DB_PASSWORD", "db.password", "1234");

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String getUser() {
        return USER;
    }

    private static String readConfig(String envName, String propertyName, String defaultValue) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue;
        }

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return defaultValue;
    }
}