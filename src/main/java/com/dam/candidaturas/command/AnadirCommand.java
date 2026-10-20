package com.dam.candidaturas.command;

import com.dam.candidaturas.model.Candidatura;
import com.dam.candidaturas.service.CandidaturaService;
import com.dam.candidaturas.view.ConsoleView;
import java.time.LocalDate;

public class AnadirCommand implements Command {
    private final CandidaturaService service;
    private final ConsoleView view;
    private final String titulo;
    private final String empresa;
    private final String estado;
    private final LocalDate fecha;

    public AnadirCommand(CandidaturaService service, ConsoleView view, String titulo, String empresa, String estado, LocalDate fecha) {
        this.service = service;
        this.view = view;
        this.titulo = titulo;
        this.empresa = empresa;
        this.estado = estado;
        this.fecha = fecha;
    }

    @Override
    public void execute() throws Exception {
        Candidatura c = service.anadir(titulo, empresa, estado, fecha);
        view.mostrarMensaje("Candidatura añadida con éxito con ID: " + c.getId());
    }
}