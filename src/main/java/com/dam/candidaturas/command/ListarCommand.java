package com.dam.candidaturas.command;

import com.dam.candidaturas.service.CandidaturaService;
import com.dam.candidaturas.view.ConsoleView;

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