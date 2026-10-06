package ni.edu.uam.practica.models.utils;

import java.sql.SQLException;

public final class DatabaseErrorFormatter {

    private DatabaseErrorFormatter() {
    }

    public static String format(String action, SQLException e) {
        if ("28P01".equals(e.getSQLState())) {
            return "No se pudo conectar a PostgreSQL: la contraseña del usuario "
                    + DatabaseConnection.getUser()
                    + " no es correcta. Actualiza DB_PASSWORD o db.password.";
        }

        return "Error al " + action + ": " + e.getMessage();
    }
}
