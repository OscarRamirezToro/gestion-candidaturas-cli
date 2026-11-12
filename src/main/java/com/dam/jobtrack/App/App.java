package com.dam.jobtrack.App;

import com.dam.jobtrack.controller.ListadoOfertasController;
import com.dam.jobtrack.model.Oferta;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    public static void abrirDetalleOferta(Oferta oferta) {
        // Conexión para el Commit 5
    }

    public static void abrirFormularioOferta(Oferta oferta, ListadoOfertasController listener) {
        // Conexión para el Commit 6
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