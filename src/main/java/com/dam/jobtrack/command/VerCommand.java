package com.dam.jobtrack.command;

import com.dam.jobtrack.model.Candidatura;
import com.dam.jobtrack.service.CandidaturaService;
import com.dam.jobtrack.view.ConsoleView;
import java.util.Optional;

public class VerCommand implements Command {
    private final CandidaturaService service;
    private final ConsoleView view;
    private final int id;

    public VerCommand(CandidaturaService service, ConsoleView view, int id) {
        this.service = service;
        this.view = view;
        this.id = id;
    }

    @Override
    public void execute() throws Exception {
        Optional<Candidatura> candidatura = service.ver(id);
        if (candidatura.isPresent()) {
            view.mostrarCandidatura(candidatura.get());
        } else {
            view.mostrarError("No se encontró ninguna candidatura con el ID " + id);
        }
    }
}