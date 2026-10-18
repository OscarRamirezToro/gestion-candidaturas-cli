package com.dam.candidaturas.repository;

import com.dam.candidaturas.model.Candidatura;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileCandidaturaRepository implements CandidaturaRepository {
    private final Path filePath;

    public FileCandidaturaRepository(Path filePath) {
        this.filePath = filePath;
        asegurarArchivoExiste();
    }

    private void asegurarArchivoExiste() {
        try {
            if (!Files.exists(filePath)) {
                if (filePath.getParent() != null) {
                    Files.createDirectories(filePath.getParent());
                }
                Files.createFile(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el archivo de datos: " + filePath, e);
        }
    }

    @Override
    public List<Candidatura> findAll() throws IOException {
        List<Candidatura> lista = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (!linea.isBlank()) {
                    lista.add(Candidatura.fromCsvLine(linea));
                }
            }
        }
        return lista;
    }

    @Override
    public Optional<Candidatura> findById(int id) throws IOException {
        return findAll().stream()
                .filter(c -> c.getId() == id)
                .findFirst();
    }

    @Override
    public void save(Candidatura candidatura) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(candidatura.toCsvLine());
            writer.newLine();
        }
    }

    @Override
    public boolean deleteById(int id) throws IOException {
        List<Candidatura> candidaturas = findAll();
        boolean existia = candidaturas.removeIf(c -> c.getId() == id);
        if (existia) {
            try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardOpenOption.TRUNCATE_EXISTING)) {
                for (Candidatura c : candidaturas) {
                    writer.write(c.toCsvLine());
                    writer.newLine();
                }
            }
        }
        return existia;
    }

    @Override
    public int getNextId() throws IOException {
        return findAll().stream()
                .mapToInt(Candidatura::getId)
                .max()
                .orElse(0) + 1;
    }
}