package ni.edu.uam.practica.dao;

import ni.edu.uam.practica.models.Categoria;
import ni.edu.uam.practica.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDAO {

    public void guardar(Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setBoolean(2, categoria.isActiva());
            ps.executeUpdate();
        }
    }

    public List<Categoria> listar() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id, nombre, activa FROM categoria ORDER BY id";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }
        }

        return categorias;
    }

    public Optional<Categoria> buscar(int id) throws SQLException {
        String sql = "SELECT id, nombre, activa FROM categoria WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearCategoria(rs));
                }
            }
        }

        return Optional.empty();
    }

    public void actualizar(Categoria categoria) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ?, activa = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, categoria.getNombre());
            ps.setBoolean(2, categoria.isActiva());
            ps.setInt(3, categoria.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Categoria mapearCategoria(ResultSet rs) throws SQLException {
        return new Categoria(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getBoolean("activa")
        );
    }
}
