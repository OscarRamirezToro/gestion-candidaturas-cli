package com.dam.jobtrack.model;

import java.time.LocalDate;

public record EntrevistaResumen(
        Integer id,
        LocalDate fecha,
        String tipo,
        String resultado,
        String notas
) {
}