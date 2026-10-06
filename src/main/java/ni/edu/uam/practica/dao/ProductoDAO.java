package ni.edu.uam.practica.dao;

import ni.edu.uam.practica.models.Categoria;
import ni.edu.uam.practica.models.Producto;
import ni.edu.uam.practica.models.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDAO {

    public void guardar(Producto producto) throws SQLException {
        String sql = """
                INSERT INTO producto
                (
                    codigo,
                    nombre,
                    categoria_id,
                    precio_venta,
                    existencia,
                    foto_ruta,
                    activo
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getFotoRuta());
            ps.setBoolean(7, producto.isActivo());
            ps.executeUpdate();
        }
    }

    public List<Producto> listar() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = """
                SELECT p.id, p.codigo, p.nombre, p.precio_venta, p.existencia, p.foto_ruta, p.activo,
                       c.id AS categoria_id, c.nombre AS categoria_nombre, c.activa AS categoria_activa
                FROM producto p
                INNER JOIN categoria c ON c.id = p.categoria_id
                ORDER BY p.id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        }

        return productos;
    }

    public Optional<Producto> buscar(int id) throws SQLException {
        String sql = """
                SELECT p.id, p.codigo, p.nombre, p.precio_venta, p.existencia, p.foto_ruta, p.activo,
                       c.id AS categoria_id, c.nombre AS categoria_nombre, c.activa AS categoria_activa
                FROM producto p
                INNER JOIN categoria c ON c.id = p.categoria_id
                WHERE p.id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearProducto(rs));
                }
            }
        }

        return Optional.empty();
    }

    public boolean existeCodigo(String codigo, Integer idExcluir) throws SQLException {
        String sql = idExcluir == null
                ? """
                    SELECT COUNT(*)
                    FROM producto
                    WHERE LOWER(codigo) = LOWER(?)
                    """
                : """
                    SELECT COUNT(*)
                    FROM producto
                    WHERE LOWER(codigo) = LOWER(?)
                      AND id <> ?
                    """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, codigo);
            if (idExcluir != null) {
                ps.setInt(2, idExcluir);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    public void actualizar(Producto producto) throws SQLException {
        String sql = """
                UPDATE producto
                SET codigo = ?,
                    nombre = ?,
                    categoria_id = ?,
                    precio_venta = ?,
                    existencia = ?,
                    foto_ruta = ?,
                    activo = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, producto.getCodigo());
            ps.setString(2, producto.getNombre());
            ps.setInt(3, producto.getCategoria().getId());
            ps.setBigDecimal(4, producto.getPrecioVenta());
            ps.setInt(5, producto.getExistencia());
            ps.setString(6, producto.getFotoRuta());
            ps.setBoolean(7, producto.isActivo());
            ps.setInt(8, producto.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(
                rs.getInt("categoria_id"),
                rs.getString("categoria_nombre"),
                rs.getBoolean("categoria_activa")
        );

        return new Producto(
                rs.getInt("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                categoria,
                rs.getBigDecimal("precio_venta"),
                rs.getInt("existencia"),
                rs.getString("foto_ruta"),
                rs.getBoolean("activo")
        );
    }
}
