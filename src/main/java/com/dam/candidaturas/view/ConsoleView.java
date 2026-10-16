package com.dam.candidaturas.view;

import com.dam.candidaturas.model.Candidatura;
import java.util.List;

public class ConsoleView {

    public void mostrarMensaje(String mensaje) {
        System.out.println(mensaje);
    }

    public void mostrarError(String error) {
        System.err.println("ERROR: " + error);
    }

    public void mostrarCandidatura(Candidatura c) {
        System.out.println("ID: " + c.getId());
        System.out.println("Título: " + c.getTitulo());
        System.out.println("Empresa: " + c.getEmpresa());
        System.out.println("Estado: " + c.getEstado());
        System.out.println("Fecha: " + c.getFecha().format(Candidatura.FORMATO_FECHA));
    }

    public void mostrarListado(List<Candidatura> candidaturas) {
        if (candidaturas.isEmpty()) {
            System.out.println("No se encontraron candidaturas.");
            return;
        }
        System.out.printf("%-5s | %-25s | %-20s | %-12s | %-10s%n", "ID", "TÍTULO", "EMPRESA", "ESTADO", "FECHA");
        System.out.println("-".repeat(82));
        for (Candidatura c : candidaturas) {
            System.out.printf("%-5d | %-25s | %-20s | %-12s | %-10s%n",
                    c.getId(),
                    c.getTitulo(),
                    c.getEmpresa(),
                    c.getEstado(),
                    c.getFecha().format(Candidatura.FORMATO_FECHA));
        }
    }
}