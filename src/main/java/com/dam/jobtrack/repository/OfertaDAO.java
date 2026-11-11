package com.dam.jobtrack.repository;

import com.dam.jobtrack.model.Oferta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface OfertaDAO extends DAO<Oferta, Integer> {
    List<Oferta> findByEstado(String estado);
    List<Oferta> findByEmpresaId(Integer idEmpresa);
    List<Oferta> findByFiltros(String estado, String nombreEmpresa, BigDecimal salarioMin, LocalDate fechaDesde);
    void updateEstado(Integer idOferta, String nuevoEstado);

    // Método transaccional para operaciones compuestas
    void guardarOfertaConTransaccion(Connection conn, Oferta oferta) throws SQLException;
}