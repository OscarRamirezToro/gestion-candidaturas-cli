package com.dam.candidaturas.service;

import com.dam.candidaturas.model.Candidatura;
import com.dam.candidaturas.repository.CandidaturaRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CandidaturaService {
    private final CandidaturaRepository repository;

    public CandidaturaService(CandidaturaRepository repository) {
        this.repository = repository;
    }

    public List<Candidatura> listar(String filtroEstado, String filtroEmpresa) throws IOException {
        List<Candidatura> candidaturas = repository.findAll();
        return candidaturas.stream()
                .filter(c -> filtroEstado == null || c.getEstado().equalsIgnoreCase(filtroEstado))
                .filter(c -> filtroEmpresa == null || c.getEmpresa().toLowerCase().contains(filtroEmpresa.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Candidatura anadir(String titulo, String empresa, String estado, LocalDate fecha) throws IOException {
        int id = repository.getNextId();
        Candidatura nueva = new Candidatura(id, titulo, empresa, estado, fecha);
        repository.save(nueva);
        return nueva;
    }

    public Optional<Candidatura> ver(int id) throws IOException {
        return repository.findById(id);
    }

    public boolean borrar(int id) throws IOException {
        return repository.deleteById(id);
    }

    public int importarCsv(Path archivoCsv) throws IOException {
        if (!Files.exists(archivoCsv)) {
            throw new IllegalArgumentException("El archivo a importar no existe: " + archivoCsv);
        }
        int contador = 0;
        try (BufferedReader reader = Files.newBufferedReader(archivoCsv)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.isBlank()) continue;
                String[] partes = linea.contains(";") ? linea.split(";") : linea.split(",");
                if (partes.length >= 4) {
                    // Admite CSV con o sin cabecera/id
                    String titulo = partes[0].trim();
                    String empresa = partes[1].trim();
                    String estado = partes[2].trim();
                    LocalDate fecha = LocalDate.parse(partes[3].trim(), Candidatura.FORMATO_FECHA);
                    anadir(titulo, empresa, estado, fecha);
                    contador++;
                }
            }
        }
        return contador;
    }
}