package com.dam.candidaturas.command;

import com.dam.candidaturas.model.Candidatura;
import com.dam.candidaturas.service.CandidaturaService;
import com.dam.candidaturas.view.ConsoleView;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class CommandFactory {
    private final CandidaturaService service;
    private final ConsoleView view;

    public CommandFactory(CandidaturaService service, ConsoleView view) {
        this.service = service;
        this.view = view;
    }

    public Command parse(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Debe proporcionar una operación (listar, añadir, ver, borrar, importar).");
        }

        String operacion = args[0].toLowerCase();

        switch (operacion) {
            case "listar":
                return parseListar(args);
            case "añadir":
            case "anadir":
                return parseAnadir(args);
            case "ver":
                return parseVer(args);
            case "borrar":
                return parseBorrar(args);
            case "importar":
                return parseImportar(args);
            default:
                throw new IllegalArgumentException("Operación desconocida: '" + operacion + "'.");
        }
    }

    private Command parseListar(String[] args) {
        String estado = null;
        String empresa = null;

        for (int i = 1; i < args.length; i++) {
            if ("--estado".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                estado = args[++i];
            } else if ("--empresa".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                empresa = args[++i];
            }
        }
        return new ListarCommand(service, view, estado, empresa);
    }

    private Command parseAnadir(String[] args) {
        Map<String, String> params = extraerParametros(args);

        String titulo = params.get("--titulo");
        String empresa = params.get("--empresa");
        String estado = params.get("--estado");
        String fechaStr = params.get("--fecha");

        if (titulo == null || empresa == null || estado == null || fechaStr == null) {
            throw new IllegalArgumentException("Faltan argumentos. Uso: añadir --titulo X --empresa Y --estado Z --fecha dd/MM/yyyy");
        }

        try {
            LocalDate fecha = LocalDate.parse(fechaStr, Candidatura.FORMATO_FECHA);
            return new AnadirCommand(service, view, titulo, empresa, estado, fecha);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha debe tener el formato dd/MM/yyyy (ejemplo: 16/10/2026).");
        }
    }

    private Command parseVer(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Debe indicar el ID de la candidatura a ver. Uso: ver <id>");
        }
        try {
            int id = Integer.parseInt(args[1]);
            return new VerCommand(service, view, id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID debe ser un número entero.");
        }
    }

    private Command parseBorrar(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Debe indicar el ID de la candidatura a borrar. Uso: borrar <id>");
        }
        try {
            int id = Integer.parseInt(args[1]);
            return new BorrarCommand(service, view, id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID debe ser un número entero.");
        }
    }

    private Command parseImportar(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Debe indicar la ruta del archivo CSV. Uso: importar <ruta_archivo.csv>");
        }
        return new ImportarCommand(service, view, Path.of(args[1]));
    }

    private Map<String, String> extraerParametros(String[] args) {
        Map<String, String> mapa = new HashMap<>();
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith("--") && i + 1 < args.length) {
                mapa.put(args[i].toLowerCase(), args[++i]);
            }
        }
        return mapa;
    }
}