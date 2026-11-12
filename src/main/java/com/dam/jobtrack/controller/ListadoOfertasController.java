package com.dam.jobtrack.controller;

import com.dam.jobtrack.App.App;
import com.dam.jobtrack.config.DatabaseConfig;
import com.dam.jobtrack.model.Oferta;
import com.dam.jobtrack.repository.OfertaRepositoryJdbc;
import com.dam.jobtrack.service.OfertaService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ListadoOfertasController implements Initializable {

    @FXML private TableView<Oferta> tablaOfertas;
    @FXML private TableColumn<Oferta, Integer> colId;
    @FXML private TableColumn<Oferta, String> colTitulo;
    @FXML private TableColumn<Oferta, String> colEmpresa;
    @FXML private TableColumn<Oferta, String> colEstado;
    @FXML private TableColumn<Oferta, LocalDate> colFecha;
    @FXML private TableColumn<Oferta, BigDecimal> colSalario;

    @FXML private TextField txtFiltroEmpresa;
    @FXML private ComboBox<String> comboFiltroEstado;
    @FXML private TextField txtFiltroSalario;
    @FXML private DatePicker dpFiltroFecha;

    private final ObservableList<Oferta> masterData = FXCollections.observableArrayList();
    private OfertaService ofertaService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inyección de servicio desacoplado
        this.ofertaService = new OfertaService(new OfertaRepositoryJdbc(DatabaseConfig.getDataSource()), DatabaseConfig.getDataSource());

        configurarColumnas();
        configurarFiltros();
        cargarDatos();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colEmpresa.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getNombreEmpresa() != null ? cellData.getValue().getNombreEmpresa() : "-"
        ));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaPostulacion"));
        colSalario.setCellValueFactory(new PropertyValueFactory<>("salario"));

        tablaOfertas.setItems(masterData);
    }

    private void configurarFiltros() {
        comboFiltroEstado.setItems(FXCollections.observableArrayList("Todos", "Enviada", "En proceso", "Aceptada", "Rechazada"));
        comboFiltroEstado.setValue("Todos");
    }

    public void cargarDatos() {
        masterData.clear();
        masterData.addAll(ofertaService.obtenerTodas());
    }

    @FXML
    private void handleFiltrar() {
        try {
            String empresa = txtFiltroEmpresa.getText();
            String estado = comboFiltroEstado.getValue();
            if ("Todos".equals(estado)) {
                estado = null;
            }

            BigDecimal salarioMin = null;
            if (txtFiltroSalario.getText() != null && !txtFiltroSalario.getText().isBlank()) {
                salarioMin = new BigDecimal(txtFiltroSalario.getText().trim());
            }

            LocalDate fecha = dpFiltroFecha.getValue();

            List<Oferta> filtradas = ofertaService.filtrarOfertas(estado, empresa, salarioMin, fecha);
            masterData.clear();
            masterData.addAll(filtradas);
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Dato inválido", "El salario debe ser un número válido.");
        }
    }

    @FXML
    private void handleLimpiarFiltros() {
        txtFiltroEmpresa.clear();
        txtFiltroSalario.clear();
        dpFiltroFecha.setValue(null);
        comboFiltroEstado.setValue("Todos");
        cargarDatos();
    }

    @FXML
    private void handleNuevaOferta() {
        App.abrirFormularioOferta(null, this);
    }

    @FXML
    private void handleVerDetalle() {
        Oferta seleccionada = tablaOfertas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Selecciona una oferta de la tabla.");
            return;
        }
        App.abrirDetalleOferta(seleccionada, this);
    }

    @FXML
    private void handleCambiarEstado() {
        Oferta seleccionada = tablaOfertas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Selecciona una oferta para cambiar su estado.");
            return;
        }

        ChoiceDialog<String> dialogo = new ChoiceDialog<>(seleccionada.getEstado(), "Enviada", "En proceso", "Aceptada", "Rechazada");
        dialogo.setTitle("Actualizar Estado");
        dialogo.setHeaderText("Candidatura #" + seleccionada.getId() + " - " + seleccionada.getTitulo());
        dialogo.setContentText("Selecciona el nuevo estado:");

        Optional<String> resultado = dialogo.showAndWait();
        resultado.ifPresent(nuevoEstado -> {
            ofertaService.cambiarEstado(seleccionada.getId(), nuevoEstado);
            cargarDatos();
        });
    }

    @FXML
    private void handleEliminar() {
        Oferta seleccionada = tablaOfertas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida", "Selecciona una oferta para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Seguro que deseas eliminar la postulación '" + seleccionada.getTitulo() + "'?");

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            ofertaService.eliminarOferta(seleccionada.getId());
            cargarDatos();
        }
    }

    @FXML
    private void handleImportarCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo CSV de ofertas");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV (*.csv)", "*.csv"));

        File archivo = fileChooser.showOpenDialog(tablaOfertas.getScene().getWindow());
        if (archivo != null) {
            try {
                int total = ofertaService.importarDesdeCsv(archivo.getAbsolutePath());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Importación completada", "Se han importado " + total + " registros con éxito.");
                cargarDatos();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de importación", e.getMessage());
            }
        }
    }

    @FXML
    private void handleAbrirEmpresas() {
        App.abrirVentanaEmpresas();
    }

    @FXML
    private void handleAbrirEntrevistas() {
        App.abrirVentanaEntrevistas();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}