package com.dam.candidaturas.command;

import com.dam.candidaturas.service.CandidaturaService;
import com.dam.candidaturas.view.ConsoleView;
import java.nio.file.Path;

public class ImportarCommand implements Command {
    private final CandidaturaService service;
    private final ConsoleView view;
    private final Path rutaCsv;

    public ImportarCommand(CandidaturaService service, ConsoleView view, Path rutaCsv) {
        this.service = service;
        this.view = view;
        this.rutaCsv = rutaCsv;
    }

    @Override
    public void execute() throws Exception {
        int total = service.importarCsv(rutaCsv);
        view.mostrarMensaje("Se han importado " + total + " candidaturas con éxito.");
    }
}