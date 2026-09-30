package ni.edu.uam.practica.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.practica.dao.CategoriaDAO;
import ni.edu.uam.practica.models.Categoria;
import ni.edu.uam.practica.utils.DatabaseErrorFormatter;

import java.sql.SQLException;

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
        if (nombreField.getText().isBlank()) {
            mostrarMensaje("Ingrese el nombre de la categoría.", true);
            return;
        }

        try {
            Categoria categoria = new Categoria(null, nombreField.getText().trim(), activaCheck.isSelected());
            categoriaDAO.guardar(categoria);
            mostrarMensaje("Categoría guardada correctamente.", false);
            limpiarFormulario();
            cargarCategorias();
        } catch (SQLException e) {
            mostrarMensaje(DatabaseErrorFormatter.format("guardar", e), true);
        }
    }

    @FXML
    private void actualizarCategoria() {
        if (idField.getText().isBlank()) {
            mostrarMensaje("Seleccione una categoría para actualizar.", true);
            return;
        }

        try {
            Categoria categoria = new Categoria(
                    Integer.parseInt(idField.getText()),
                    nombreField.getText().trim(),
                    activaCheck.isSelected()
            );
            categoriaDAO.actualizar(categoria);
            mostrarMensaje("Categoría actualizada correctamente.", false);
            limpiarFormulario();
            cargarCategorias();
        } catch (NumberFormatException e) {
            mostrarMensaje("El id de la categoría no es válido.", true);
        } catch (SQLException e) {
            mostrarMensaje(DatabaseErrorFormatter.format("actualizar", e), true);
        }
    }

    @FXML
    private void eliminarCategoria() {
        if (idField.getText().isBlank()) {
            mostrarMensaje("Seleccione una categoría para eliminar.", true);
            return;
        }

        try {
            categoriaDAO.eliminar(Integer.parseInt(idField.getText()));
            mostrarMensaje("Categoría eliminada correctamente.", false);
            limpiarFormulario();
            cargarCategorias();
        } catch (NumberFormatException e) {
            mostrarMensaje("El id de la categoría no es válido.", true);
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
            mostrarMensaje(DatabaseErrorFormatter.format("listar categorías", e), true);
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

    private void mostrarMensaje(String mensaje, boolean error) {
        resultadoLabel.setText(mensaje);
    }
}
