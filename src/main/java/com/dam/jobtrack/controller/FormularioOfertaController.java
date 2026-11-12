package com.dam.jobtrack.controller;

import com.dam.jobtrack.config.DatabaseConfig;
import com.dam.jobtrack.exception.DatabaseException;
import com.dam.jobtrack.model.EmpresaOpcion;
import com.dam.jobtrack.model.Oferta;
import com.dam.jobtrack.repository.OfertaRepositoryJdbc;
import com.dam.jobtrack.service.OfertaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class FormularioOfertaController {

    @FXML private Label lblTituloFormulario;
    @FXML private TextField txtTitulo;
    @FXML private ComboBox<EmpresaOpcion> comboEmpresa;
    @FXML private ComboBox<String> comboEstado;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtSalario;
    @FXML private TextField txtEnlace;
    @FXML private TextArea txtDescripcion;
    @FXML private TextArea txtNotas;
    @FXML private Button btnGuardar;

    private Oferta ofertaEdicion;
    private ListadoOfertasController listadoController;
    private OfertaService ofertaService;

    @FXML
    private void initialize() {
        this.ofertaService = new OfertaService(
                new OfertaRepositoryJdbc(DatabaseConfig.getDataSource()),
                DatabaseConfig.getDataSource()
        );

        comboEstado.setItems(FXCollections.observableArrayList("Enviada", "En proceso", "Aceptada", "Rechazada"));
        comboEstado.setValue("Enviada");
        dpFecha.setValue(LocalDate.now());

        cargarEmpresasDisponibles();

        // Diseño defensivo: deshabilitar botón Guardar si falta título o empresa (Tema 1 y Tema 2)
        btnGuardar.disableProperty().bind(
                txtTitulo.textProperty().isEmpty()
                        .or(comboEmpresa.valueProperty().isNull())
        );
    }

    private void cargarEmpresasDisponibles() {
        ObservableList<EmpresaOpcion> opciones = FXCollections.observableArrayList();
        String sql = "SELECT id, nombre FROM EMPRESA ORDER BY nombre ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                opciones.add(new EmpresaOpcion(rs.getInt("id"), rs.getString("nombre")));
            }
            comboEmpresa.setItems(opciones);
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo cargar el listado de empresas", e);
        }
    }

    public void setDatos(Oferta oferta, ListadoOfertasController listadoController) {
        this.ofertaEdicion = oferta;
        this.listadoController = listadoController;

        if (oferta != null) {
            lblTituloFormulario.setText("Editar Candidatura #" + oferta.getId());
            txtTitulo.setText(oferta.getTitulo());
            comboEstado.setValue(oferta.getEstado());
            dpFecha.setValue(oferta.getFechaPostulacion());
            txtEnlace.setText(oferta.getEnlace() != null ? oferta.getEnlace() : "");
            txtDescripcion.setText(oferta.getDescripcion() != null ? oferta.getDescripcion() : "");
            txtNotas.setText(oferta.getNotas() != null ? oferta.getNotas() : "");

            if (oferta.getSalario() != null) {
                txtSalario.setText(oferta.getSalario().toPlainString());
            }

            // Seleccionar empresa correspondiente en el ComboBox
            comboEmpresa.getItems().stream()
                    .filter(emp -> emp.id().equals(oferta.getIdEmpresa()))
                    .findFirst()
                    .ifPresent(comboEmpresa::setValue);
        }
    }

    @FXML
    private void handleGuardar() {
        try {
            String titulo = txtTitulo.getText().trim();
            EmpresaOpcion empresaSeleccionada = comboEmpresa.getValue();
            String estado = comboEstado.getValue();
            LocalDate fecha = dpFecha.getValue();
            String enlace = txtEnlace.getText() != null ? txtEnlace.getText().trim() : "";
            String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "";
            String notas = txtNotas.getText() != null ? txtNotas.getText().trim() : "";

            if (fecha == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Fecha requerida", "Debes seleccionar una fecha de postulación.");
                return;
            }

            if (!enlace.isBlank() && !enlace.startsWith("http://") && !enlace.startsWith("https://")) {
                mostrarAlerta(Alert.AlertType.WARNING, "Enlace inválido", "La URL debe comenzar por http:// o https://");
                return;
            }

            BigDecimal salario = null;
            if (txtSalario.getText() != null && !txtSalario.getText().isBlank()) {
                salario = new BigDecimal(txtSalario.getText().trim().replace(",", "."));
                if (salario.compareTo(BigDecimal.ZERO) < 0) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Salario inválido", "El salario no puede ser negativo.");
                    return;
                }
            }

            if (ofertaEdicion == null) {
                Oferta nueva = new Oferta(
                        null, empresaSeleccionada.id(), titulo, descripcion,
                        estado, fecha, enlace, salario, notas
                );
                ofertaService.registrarOferta(nueva);
            } else {
                ofertaEdicion.setIdEmpresa(empresaSeleccionada.id());
                ofertaEdicion.setTitulo(titulo);
                ofertaEdicion.setDescripcion(descripcion);
                ofertaEdicion.setEstado(estado);
                ofertaEdicion.setFechaPostulacion(fecha);
                ofertaEdicion.setEnlace(enlace);
                ofertaEdicion.setSalario(salario);
                ofertaEdicion.setNotas(notas);
                ofertaService.actualizarOferta(ofertaEdicion);
            }

            if (listadoController != null) {
                listadoController.cargarDatos();
            }
            cerrarVentana();

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Formato numérico incorrecto", "Introduce un salario numérico válido (ej: 25000).");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar", e.getMessage());
        }
    }

    @FXML
    private void handleCancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtTitulo.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}