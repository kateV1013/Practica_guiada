package ni.edu.uam.practica.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.practica.dao.CategoriaDAO;
import ni.edu.uam.practica.models.Categoria;
import ni.edu.uam.practica.models.utils.DatabaseErrorFormatter;

import java.sql.SQLException;
import java.util.Optional;

public class CategoriaController {
    @FXML private TextField idField;
    @FXML private TextField nombreField;
    @FXML private CheckBox activaCheck;
    @FXML private TableView<Categoria> categoriaTable;
    @FXML private TableColumn<Categoria, Integer> idColumn;
    @FXML private TableColumn<Categoria, String> nombreColumn;
    @FXML private TableColumn<Categoria, Boolean> activaColumn;
    @FXML private Label resultadoLabel;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        activaColumn.setCellValueFactory(new PropertyValueFactory<>("activa"));
        categoriaTable.setItems(categorias);
        categoriaTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionada) -> mostrarCategoria(seleccionada)
        );
        activaCheck.setSelected(true);
        cargarCategorias();
    }

    @FXML
    private void guardarCategoria() {
        try {
            Categoria categoria = leerCategoriaFormulario(null);
            if (categoriaDAO.existeNombre(categoria.getNombre(), null)) {
                mostrarMensaje("Ya existe una categoria con ese nombre.", true);
                return;
            }

            categoriaDAO.guardar(categoria);
            mostrarMensaje("Categoria guardada correctamente.", false);
            limpiarFormulario();
            cargarCategorias();
        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), true);
        } catch (SQLException e) {
            mostrarMensaje(DatabaseErrorFormatter.format("guardar", e), true);
        }
    }

    @FXML
    private void actualizarCategoria() {
        if (idField.getText().isBlank()) {
            mostrarMensaje("Seleccione una categoria para actualizar.", true);
            return;
        }

        try {
            int id = Integer.parseInt(idField.getText());
            Categoria categoria = leerCategoriaFormulario(id);
            if (categoriaDAO.existeNombre(categoria.getNombre(), id)) {
                mostrarMensaje("Ya existe una categoria con ese nombre.", true);
                return;
            }

            categoriaDAO.actualizar(categoria);
            mostrarMensaje("Categoria actualizada correctamente.", false);
            limpiarFormulario();
            cargarCategorias();
        } catch (NumberFormatException e) {
            mostrarMensaje("El id de la categoria no es valido.", true);
        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), true);
        } catch (SQLException e) {
            mostrarMensaje(DatabaseErrorFormatter.format("actualizar", e), true);
        }
    }

    @FXML
    private void eliminarCategoria() {
        if (idField.getText().isBlank()) {
            mostrarMensaje("Seleccione una categoria para eliminar.", true);
            return;
        }

        try {
            int id = Integer.parseInt(idField.getText());
            Categoria categoriaSeleccionada = categoriaTable.getSelectionModel().getSelectedItem();
            if (categoriaDAO.tieneProductos(id)) {
                mostrarMensaje("No puede eliminar la categoria porque tiene productos asociados.", true);
                return;
            }

            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar eliminacion");
            confirmacion.setHeaderText("Eliminar categoria");
            confirmacion.setContentText("Desea eliminar la categoria "
                    + (categoriaSeleccionada != null ? categoriaSeleccionada.getNombre() : id)
                    + "?");

            Optional<ButtonType> respuesta = confirmacion.showAndWait();
            if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
                return;
            }

            categoriaDAO.eliminar(id);
            mostrarMensaje("Categoria eliminada correctamente.", false);
            limpiarFormulario();
            cargarCategorias();
        } catch (NumberFormatException e) {
            mostrarMensaje("El id de la categoria no es valido.", true);
        } catch (SQLException e) {
            mostrarMensaje(DatabaseErrorFormatter.format("eliminar", e), true);
        }
    }

    @FXML
    private void limpiarFormulario() {
        idField.clear();
        nombreField.clear();
        activaCheck.setSelected(true);
        categoriaTable.getSelectionModel().clearSelection();
    }

    private void cargarCategorias() {
        try {
            categorias.setAll(categoriaDAO.listar());
        } catch (SQLException e) {
            mostrarMensaje(DatabaseErrorFormatter.format("listar categorias", e), true);
        }
    }

    private void mostrarCategoria(Categoria categoria) {
        if (categoria == null) {
            return;
        }

        idField.setText(String.valueOf(categoria.getId()));
        nombreField.setText(categoria.getNombre());
        activaCheck.setSelected(categoria.isActiva());
    }

    private Categoria leerCategoriaFormulario(Integer id) {
        String nombre = nombreField.getText().trim();

        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoria es obligatorio.");
        }

        return new Categoria(id, nombre, activaCheck.isSelected());
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        resultadoLabel.setText(mensaje);
    }
}
