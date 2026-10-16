package com.dam.candidaturas.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Candidatura {
    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final int id;
    private final String titulo;
    private final String empresa;
    private final String estado;
    private final LocalDate fecha;

    public Candidatura(int id, String titulo, String empresa, String estado, LocalDate fecha) {
        this.id = id;
        this.titulo = titulo;
        this.empresa = empresa;
        this.estado = estado;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getEmpresa() {
        return empresa;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String toCsvLine() {
        return id + ";" + titulo + ";" + empresa + ";" + estado + ";" + fecha.format(FORMATO_FECHA);
    }

    public static Candidatura fromCsvLine(String linea) {
        String[] partes = linea.split(";");
        if (partes.length < 5) {
            throw new IllegalArgumentException("Línea con formato inválido: " + linea);
        }
        int id = Integer.parseInt(partes[0].trim());
        String titulo = partes[1].trim();
        String empresa = partes[2].trim();
        String estado = partes[3].trim();
        LocalDate fecha = LocalDate.parse(partes[4].trim(), FORMATO_FECHA);
        return new Candidatura(id, titulo, empresa, estado, fecha);
    }
}