package com.dam.jobtrack.command;

import com.dam.jobtrack.service.CandidaturaService;
import com.dam.jobtrack.view.ConsoleView;

public class ListarCommand implements Command {
    private final CandidaturaService service;
    private final ConsoleView view;
    private final String estado;
    private final String empresa;

    public ListarCommand(CandidaturaService service, ConsoleView view, String estado, String empresa) {
        this.service = service;
        this.view = view;
        this.estado = estado;
        this.empresa = empresa;
    }

    @Override
    public void execute() throws Exception {
        view.mostrarListado(service.listar(estado, empresa));
    }
}