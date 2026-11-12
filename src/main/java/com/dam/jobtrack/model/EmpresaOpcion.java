package com.dam.jobtrack.model;

public record EmpresaOpcion(Integer id, String nombre) {
    @Override
    public String toString() {
        return nombre;
    }
}