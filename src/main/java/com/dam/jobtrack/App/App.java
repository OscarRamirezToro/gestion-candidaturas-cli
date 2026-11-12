package com.dam.jobtrack.App;

import com.dam.jobtrack.controller.DetalleOfertaController;
import com.dam.jobtrack.controller.FormularioOfertaController;
import com.dam.jobtrack.controller.ListadoOfertasController;
import com.dam.jobtrack.model.Oferta;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/dam/jobtrack/view/ListadoOfertas.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setTitle("JobTrack - Sistema de Gestión de Candidaturas (v2.0)");
        stage.setScene(scene);
        stage.setMinWidth(950);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void abrirDetalleOferta(Oferta oferta, ListadoOfertasController listener) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/dam/jobtrack/view/DetalleOferta.fxml"));
            Parent root = loader.load();

            DetalleOfertaController controller = loader.getController();
            controller.setOferta(oferta, listener);

            Stage modal = new Stage();
            modal.setTitle("Detalle de Candidatura #" + oferta.getId());
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(primaryStage);
            modal.setScene(new Scene(root));
            modal.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo cargar la ventana de detalle.");
            alert.showAndWait();
        }
    }

    public static void abrirFormularioOferta(Oferta oferta, ListadoOfertasController listener) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/dam/jobtrack/view/FormularioOferta.fxml"));
            Parent root = loader.load();

            FormularioOfertaController controller = loader.getController();
            controller.setDatos(oferta, listener);

            Stage modal = new Stage();
            modal.setTitle(oferta == null ? "Nueva Candidatura" : "Editar Candidatura #" + oferta.getId());
            modal.initModality(Modality.WINDOW_MODAL);
            modal.initOwner(primaryStage);
            modal.setScene(new Scene(root));
            modal.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo abrir el formulario de ofertas.");
            alert.showAndWait();
        }
    }

    public static void abrirVentanaEmpresas() {
        // Módulo asignado al compañero Juan Antonio
    }

    public static void abrirVentanaEntrevistas() {
        // Módulo asignado al compañero Juan Antonio
    }

    public static void main(String[] args) {
        launch(args);
    }
}