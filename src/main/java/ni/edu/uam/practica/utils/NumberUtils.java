package ni.edu.uam.practica.utils;

public final class NumberUtils {
    private NumberUtils() {
    }

    public static int leerEntero(String valor) {
        if (valor == null || valor.isBlank()) {
            return 0;
        }

        return Integer.parseInt(valor.trim());
    }

    public static double leerDecimal(String valor) {
        if (valor == null || valor.isBlank()) {
            return 0;
        }

        return Double.parseDouble(valor.trim());
    }
}
