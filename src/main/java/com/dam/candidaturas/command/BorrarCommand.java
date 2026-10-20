package com.dam.candidaturas.command;

import com.dam.candidaturas.service.CandidaturaService;
import com.dam.candidaturas.view.ConsoleView;

public class BorrarCommand implements Command {
    private final CandidaturaService service;
    private final ConsoleView view;
    private final int id;

    public BorrarCommand(CandidaturaService service, ConsoleView view, int id) {
        this.service = service;
        this.view = view;
        this.id = id;
    }

    @Override
    public void execute() throws Exception {
        boolean borrado = service.borrar(id);
        if (borrado) {
            view.mostrarMensaje("Candidatura con ID " + id + " eliminada correctamente.");
        } else {
            view.mostrarError("No se encontró ninguna candidatura con el ID " + id + " para borrar.");
        }
    }
}