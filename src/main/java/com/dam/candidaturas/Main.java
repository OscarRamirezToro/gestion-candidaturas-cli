package com.dam.candidaturas;

import com.dam.candidaturas.command.Command;
import com.dam.candidaturas.command.CommandFactory;
import com.dam.candidaturas.repository.CandidaturaRepository;
import com.dam.candidaturas.repository.FileCandidaturaRepository;
import com.dam.candidaturas.service.CandidaturaService;
import com.dam.candidaturas.view.ConsoleView;

import java.nio.file.Path;

/**
 * Sistema de Gestión de Candidaturas - v1.0
 * Módulo: Acceso a Datos & Desarrollo de Interfaces (2º DAM)
 *
 * Equipo:
 * - Óscar Ramírez Toro
 * - Juan Antonio Moreno Hevilla
 */

public class Main {
    public static void main(String[] args) {
        ConsoleView view = new ConsoleView();

        try {
            Path rutaArchivo = Path.of("candidaturas.txt");
            CandidaturaRepository repository = new FileCandidaturaRepository(rutaArchivo);
            CandidaturaService service = new CandidaturaService(repository);
            CommandFactory factory = new CommandFactory(service, view);

            Command comando = factory.parse(args);
            comando.execute();

        } catch (IllegalArgumentException e) {
            view.mostrarError(e.getMessage());
        } catch (Exception e) {
            view.mostrarError("Ocurrió un error inesperado al procesar la operación: " + e.getMessage());
        }
    }
}