package com.dam.jobtrack.controller;

import com.dam.jobtrack.App.App;
import com.dam.jobtrack.config.DatabaseConfig;
import com.dam.jobtrack.exception.DatabaseException;
import com.dam.jobtrack.model.EntrevistaResumen;
import com.dam.jobtrack.model.Oferta;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class DetalleOfertaController {

    @FXML private Label lblTituloOferta;
    @FXML private Label lblSubtituloOferta;
    @FXML private Label lblEstadoBadge;
    @FXML private Label lblSalario;
    @FXML private Hyperlink linkEnlace;
    @FXML private TextArea txtDescripcion;
    @FXML private TextArea txtNotas;

    @FXML private Label lblEmpresaNombre;
    @FXML private Label lblEmpresaSector;
    @FXML private Label lblEmpresaWeb;

    @FXML private TableView<EntrevistaResumen> tablaEntrevistas;
    @FXML private TableColumn<EntrevistaResumen, LocalDate> colEntFecha;
    @FXML private TableColumn<EntrevistaResumen, String> colEntTipo;
    @FXML private TableColumn<EntrevistaResumen, String> colEntResultado;
    @FXML private TableColumn<EntrevistaResumen, String> colEntNotas;

    private Oferta ofertaActual;
    private ListadoOfertasController listadoController;
    private final ObservableList<EntrevistaResumen> entrevistasData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colEntFecha.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().fecha()));
        colEntTipo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().tipo()));
        colEntResultado.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().resultado() != null ? cell.getValue().resultado() : "Pendiente"
        ));
        colEntNotas.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().notas()));
        tablaEntrevistas.setItems(entrevistasData);
    }

    public void setOferta(Oferta oferta, ListadoOfertasController listadoController) {
        this.ofertaActual = oferta;
        this.listadoController = listadoController;

        lblTituloOferta.setText(oferta.getTitulo());
        lblSubtituloOferta.setText("Candidatura #" + oferta.getId() + " • Postulada el " + oferta.getFechaPostulacion());
        lblEstadoBadge.setText(oferta.getEstado());
        aplicarEstiloBadge(oferta.getEstado());

        lblSalario.setText(oferta.getSalario() != null ? oferta.getSalario() + " € / año" : "No especificado");
        linkEnlace.setText((oferta.getEnlace() != null && !oferta.getEnlace().isBlank()) ? oferta.getEnlace() : "Sin enlace");
        txtDescripcion.setText(oferta.getDescripcion() != null ? oferta.getDescripcion() : "");
        txtNotas.setText(oferta.getNotas() != null ? oferta.getNotas() : "");

        cargarDatosEmpresaYEntrevistas(oferta.getIdEmpresa(), oferta.getId());
    }

    private void cargarDatosEmpresaYEntrevistas(int idEmpresa, int idOferta) {
        String sqlEmpresa = "SELECT nombre, sector, web FROM EMPRESA WHERE id = ?";
        String sqlEntrevistas = "SELECT id, fecha, tipo, resultado, notas FROM ENTREVISTA WHERE id_oferta = ? ORDER BY fecha ASC";

        try (Connection conn = DatabaseConfig.getConnection()) {
            // 1. Cargar datos de empresa
            try (PreparedStatement psEmp = conn.prepareStatement(sqlEmpresa)) {
                psEmp.setInt(1, idEmpresa);
                try (ResultSet rs = psEmp.executeQuery()) {
                    if (rs.next()) {
                        lblEmpresaNombre.setText(rs.getString("nombre"));
                        lblEmpresaSector.setText(rs.getString("sector") != null ? rs.getString("sector") : "-");
                        lblEmpresaWeb.setText(rs.getString("web") != null ? rs.getString("web") : "-");
                    }
                }
            }

            // 2. Cargar entrevistas asociadas
            entrevistasData.clear();
            try (PreparedStatement psEnt = conn.prepareStatement(sqlEntrevistas)) {
                psEnt.setInt(1, idOferta);
                try (ResultSet rs = psEnt.executeQuery()) {
                    while (rs.next()) {
                        entrevistasData.add(new EntrevistaResumen(
                                rs.getInt("id"),
                                rs.getDate("fecha").toLocalDate(),
                                rs.getString("tipo"),
                                rs.getString("resultado"),
                                rs.getString("notas")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error cargando detalle relacional de la oferta", e);
        }
    }

    private void aplicarEstiloBadge(String estado) {
        String estiloBase = "-fx-font-weight: bold; -fx-padding: 6 14 6 14; -fx-background-radius: 14px; ";
        switch (estado) {
            case "Aceptada" -> lblEstadoBadge.setStyle(estiloBase + "-fx-background-color: #DCFCE7; -fx-text-fill: #15803D;");
            case "Rechazada" -> lblEstadoBadge.setStyle(estiloBase + "-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C;");
            case "En proceso" -> lblEstadoBadge.setStyle(estiloBase + "-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8;");
            default -> lblEstadoBadge.setStyle(estiloBase + "-fx-background-color: #F1F5F9; -fx-text-fill: #475569;");
        }
    }

    @FXML
    private void handleAbrirEnlace() {
        String url = linkEnlace.getText();
        if (url != null && url.startsWith("http") && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception ignored) {
            }
        }
    }

    @FXML
    private void handleEditarOferta() {
        handleCerrar();
        App.abrirFormularioOferta(ofertaActual, listadoController);
    }

    @FXML
    private void handleCerrar() {
        Stage stage = (Stage) lblTituloOferta.getScene().getWindow();
        stage.close();
    }
}