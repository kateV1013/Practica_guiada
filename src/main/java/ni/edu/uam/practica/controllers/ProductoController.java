package ni.edu.uam.practica.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import ni.edu.uam.practica.dao.CategoriaDAO;
import ni.edu.uam.practica.dao.ProductoDAO;
import ni.edu.uam.practica.models.Categoria;
import ni.edu.uam.practica.models.Producto;
import ni.edu.uam.practica.utils.DatabaseErrorFormatter;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

public class ProductoController {
    @FXML private TextField idField;
    @FXML private TextField codigoField;
    @FXML private TextField nombreField;
    @FXML private ComboBox<Categoria> categoriaCombo;
    @FXML private TextField precioField;
    @FXML private TextField existenciaField;
    @FXML private TextField fotoRutaField;
    @FXML private ImageView fotoPreview;
    @FXML private CheckBox activoCheck;
    @FXML private TextField busquedaField;
    @FXML private ComboBox<String> estadoFiltroCombo;
    @FXML private ComboBox<Categoria> categoriaFiltroCombo;
    @FXML private TableView<Producto> productoTable;
    @FXML private TableColumn<Producto, Integer> idColumn;
    @FXML private TableColumn<Producto, String> codigoColumn;
    @FXML private TableColumn<Producto, String> nombreColumn;
    @FXML private TableColumn<Producto, String> categoriaColumn;
    @FXML private TableColumn<Producto, BigDecimal> precioColumn;
    @FXML private TableColumn<Producto, Integer> existenciaColumn;
    @FXML private TableColumn<Producto, String> fotoColumn;
    @FXML private TableColumn<Producto, Boolean> activoColumn;
    @FXML private Label resultadoLabel;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private final FilteredList<Producto> productosFiltrados = new FilteredList<>(productos, producto -> true);

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        codigoColumn.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        categoriaColumn.setCellValueFactory(new PropertyValueFactory<>("nombreCategoria"));
        precioColumn.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        existenciaColumn.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        fotoColumn.setCellValueFactory(new PropertyValueFactory<>("estadoFoto"));
        activoColumn.setCellValueFactory(new PropertyValueFactory<>("activo"));

        categoriaCombo.setItems(categorias);
        categoriaCombo.setOnShowing(event -> cargarCategorias());
        categoriaFiltroCombo.setItems(categorias);
        categoriaFiltroCombo.setOnShowing(event -> cargarCategorias());

        estadoFiltroCombo.setItems(FXCollections.observableArrayList("Todos", "Activos", "Inactivos"));
        estadoFiltroCombo.getSelectionModel().select("Todos");

        productoTable.setItems(productosFiltrados);
        productoTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionado) -> mostrarProducto(seleccionado)
        );

        busquedaField.textProperty().addListener((observable, anterior, nuevo) -> aplicarFiltros());
        estadoFiltroCombo.valueProperty().addListener((observable, anterior, nuevo) -> aplicarFiltros());
        categoriaFiltroCombo.valueProperty().addListener((observable, anterior, nuevo) -> aplicarFiltros());

        activoCheck.setSelected(true);
        cargarCategorias();
        cargarProductos();
    }

    @FXML
    private void seleccionarFoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar foto del producto");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File archivo = fileChooser.showOpenDialog(fotoRutaField.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        fotoRutaField.setText(archivo.getAbsolutePath());
        mostrarFoto(archivo.getAbsolutePath());
    }

    @FXML
    private void quitarFoto() {
        fotoRutaField.clear();
        fotoPreview.setImage(null);
    }

    @FXML
    private void guardarProducto() {
        try {
            productoDAO.guardar(leerProductoFormulario(null));
            mostrarMensaje("Producto guardado correctamente.", false);
            limpiarFormulario();
            cargarProductos();
        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), true);
        } catch (SQLException e) {
            mostrarMensaje(DatabaseErrorFormatter.format("guardar", e), true);
        }
    }

    @FXML
    private void actualizarProducto() {
        if (idField.getText().isBlank()) {
            mostrarMensaje("Seleccione un producto para actualizar.", true);
            return;
        }

        try {
            productoDAO.actualizar(leerProductoFormulario(Integer.parseInt(idField.getText())));
            mostrarMensaje("Producto actualizado correctamente.", false);
            limpiarFormulario();
            cargarProductos();
        } catch (NumberFormatException e) {
            mostrarMensaje("El id del producto no es valido.", true);
        } catch (IllegalArgumentException e) {
            mostrarMensaje(e.getMessage(), true);
        } catch (SQLException e) {
            mostrarMensaje(DatabaseErrorFormatter.format("actualizar", e), true);
        }
    }

    @FXML
    private void eliminarProducto() {
        Producto productoSeleccionado = productoTable.getSelectionModel().getSelectedItem();
        if (productoSeleccionado == null || idField.getText().isBlank()) {
            mostrarMensaje("Seleccione un producto para eliminar.", true);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminacion");
        confirmacion.setHeaderText("Eliminar producto");
        confirmacion.setContentText("Desea eliminar el producto " + productoSeleccionado.getNombre() + "?");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {
            productoDAO.eliminar(Integer.parseInt(idField.getText()));
            mostrarMensaje("Producto eliminado correctamente.", false);
            limpiarFormulario();
            cargarProductos();
        } catch (NumberFormatException e) {
            mostrarMensaje("El id del producto no es valido.", true);
        } catch (SQLException e) {
            mostrarMensaje(DatabaseErrorFormatter.format("eliminar", e), true);
        }
    }

    @FXML
    private void limpiarFormulario() {
        idField.clear();
        codigoField.clear();
        nombreField.clear();
        categoriaCombo.getSelectionModel().clearSelection();
        precioField.clear();
        existenciaField.clear();
        quitarFoto();
        activoCheck.setSelected(true);
        productoTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void limpiarFiltros() {
        busquedaField.clear();
        estadoFiltroCombo.getSelectionModel().select("Todos");
        categoriaFiltroCombo.getSelectionModel().clearSelection();
        aplicarFiltros();
    }

    private void cargarCategorias() {
        try {
            Integer categoriaSeleccionadaId = obtenerIdCategoria(categoriaCombo.getSelectionModel().getSelectedItem());
            Integer categoriaFiltradaId = obtenerIdCategoria(categoriaFiltroCombo.getSelectionModel().getSelectedItem());

            categorias.setAll(categoriaDAO.listar());
            seleccionarCategoriaPorId(categoriaCombo, categoriaSeleccionadaId);
            seleccionarCategoriaPorId(categoriaFiltroCombo, categoriaFiltradaId);
        } catch (SQLException e) {
            mostrarMensaje(DatabaseErrorFormatter.format("cargar categorias", e), true);
        }
    }

    private void cargarProductos() {
        try {
            productos.setAll(productoDAO.listar());
            aplicarFiltros();
        } catch (SQLException e) {
            mostrarMensaje(DatabaseErrorFormatter.format("listar productos", e), true);
        }
    }

    private Producto leerProductoFormulario(Integer id) {
        String codigo = codigoField.getText().trim();
        String nombre = nombreField.getText().trim();

        if (codigo.isBlank()) {
            throw new IllegalArgumentException("El codigo es obligatorio.");
        }
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        Categoria categoria = categoriaCombo.getSelectionModel().getSelectedItem();
        if (categoria == null) {
            throw new IllegalArgumentException("Seleccione una categoria.");
        }

        BigDecimal precio;
        int existencia;
        try {
            precio = new BigDecimal(precioField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser numerico.");
        }

        try {
            existencia = Integer.parseInt(existenciaField.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("La existencia debe ser un numero entero.");
        }

        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        if (existencia < 0) {
            throw new IllegalArgumentException("La existencia no puede ser negativa.");
        }
        if (existeCodigoDuplicado(codigo, id)) {
            throw new IllegalArgumentException("No se permiten codigos duplicados.");
        }

        return new Producto(
                id,
                codigo,
                nombre,
                categoria,
                precio,
                existencia,
                normalizarFotoRuta(fotoRutaField.getText()),
                activoCheck.isSelected()
        );
    }

    private boolean existeCodigoDuplicado(String codigo, Integer idActual) {
        return productos.stream()
                .anyMatch(producto -> producto.getCodigo().equalsIgnoreCase(codigo)
                        && (idActual == null || !idActual.equals(producto.getId())));
    }

    private void aplicarFiltros() {
        String busqueda = busquedaField.getText() == null
                ? ""
                : busquedaField.getText().trim().toLowerCase(Locale.ROOT);
        String estado = estadoFiltroCombo.getValue();
        Categoria categoriaSeleccionada = categoriaFiltroCombo.getSelectionModel().getSelectedItem();

        productosFiltrados.setPredicate(producto -> {
            boolean coincideBusqueda = busqueda.isBlank()
                    || contiene(producto.getCodigo(), busqueda)
                    || contiene(producto.getNombre(), busqueda)
                    || contiene(producto.getNombreCategoria(), busqueda);

            boolean coincideEstado = "Activos".equals(estado)
                    ? producto.isActivo()
                    : !"Inactivos".equals(estado) || !producto.isActivo();

            boolean coincideCategoria = categoriaSeleccionada == null
                    || (producto.getCategoria() != null
                    && categoriaSeleccionada.getId().equals(producto.getCategoria().getId()));

            return coincideBusqueda && coincideEstado && coincideCategoria;
        });
    }

    private boolean contiene(String valor, String busqueda) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(busqueda);
    }

    private void mostrarProducto(Producto producto) {
        if (producto == null) {
            return;
        }

        idField.setText(String.valueOf(producto.getId()));
        codigoField.setText(producto.getCodigo());
        nombreField.setText(producto.getNombre());
        seleccionarCategoria(producto.getCategoria());
        precioField.setText(producto.getPrecioVenta().toPlainString());
        existenciaField.setText(String.valueOf(producto.getExistencia()));
        fotoRutaField.setText(producto.getFotoRuta());
        mostrarFoto(producto.getFotoRuta());
        activoCheck.setSelected(producto.isActivo());
    }

    private void mostrarFoto(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            fotoPreview.setImage(null);
            return;
        }

        try {
            fotoPreview.setImage(new Image(new File(ruta).toURI().toString(), true));
        } catch (IllegalArgumentException e) {
            fotoPreview.setImage(null);
            mostrarMensaje("No se pudo cargar la foto seleccionada.", true);
        }
    }

    private String normalizarFotoRuta(String ruta) {
        if (ruta == null || ruta.isBlank()) {
            return null;
        }

        return ruta.trim();
    }

    private void seleccionarCategoria(Categoria categoriaProducto) {
        seleccionarCategoriaPorId(categoriaCombo, obtenerIdCategoria(categoriaProducto));
    }

    private void seleccionarCategoriaPorId(ComboBox<Categoria> comboBox, Integer categoriaId) {
        if (categoriaId == null) {
            comboBox.getSelectionModel().clearSelection();
            return;
        }

        categorias.stream()
                .filter(categoria -> categoriaId.equals(categoria.getId()))
                .findFirst()
                .ifPresent(categoria -> comboBox.getSelectionModel().select(categoria));
    }

    private Integer obtenerIdCategoria(Categoria categoria) {
        return categoria != null ? categoria.getId() : null;
    }

    private void mostrarMensaje(String mensaje, boolean error) {
        resultadoLabel.setText(mensaje);
    }
}
