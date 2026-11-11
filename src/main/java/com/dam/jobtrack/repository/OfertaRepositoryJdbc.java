package com.dam.jobtrack.repository;

import com.dam.jobtrack.exception.DatabaseException;
import com.dam.jobtrack.model.Oferta;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OfertaRepositoryJdbc implements OfertaDAO {

    private final DataSource dataSource;

    public OfertaRepositoryJdbc(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Oferta mapRow(ResultSet rs) throws SQLException {
        Oferta oferta = new Oferta();
        oferta.setId(rs.getInt("id"));
        oferta.setIdEmpresa(rs.getInt("id_empresa"));
        oferta.setNombreEmpresa(rs.getString("empresa_nombre")); // Obtenido vía JOIN
        oferta.setTitulo(rs.getString("titulo"));
        oferta.setDescripcion(rs.getString("descripcion"));
        oferta.setEstado(rs.getString("estado"));

        Date fechaSql = rs.getDate("fechaPostulacion");
        if (fechaSql != null) {
            oferta.setFechaPostulacion(fechaSql.toLocalDate());
        }

        oferta.setEnlace(rs.getString("enlace"));
        oferta.setSalario(rs.getBigDecimal("salario"));
        oferta.setNotas(rs.getString("notas"));
        return oferta;
    }

    @Override
    public Oferta save(Oferta entity) {
        String sql = "INSERT INTO OFERTA (id_empresa, titulo, descripcion, estado, fechaPostulacion, enlace, salario, notas) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, entity.getIdEmpresa());
            pstmt.setString(2, entity.getTitulo());
            pstmt.setString(3, entity.getDescripcion());
            pstmt.setString(4, entity.getEstado());
            pstmt.setDate(5, Date.valueOf(entity.getFechaPostulacion()));
            pstmt.setString(6, entity.getEnlace());
            pstmt.setBigDecimal(7, entity.getSalario());
            pstmt.setString(8, entity.getNotas());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entity.setId(generatedKeys.getInt(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new DatabaseException("Error al insertar la oferta laboral", e);
        }
    }

    @Override
    public Optional<Oferta> findById(Integer id) {
        String sql = "SELECT o.*, e.nombre AS empresa_nombre " +
                "FROM OFERTA o " +
                "INNER JOIN EMPRESA e ON o.id_empresa = e.id " +
                "WHERE o.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error al consultar oferta por ID: " + id, e);
        }
    }

    @Override
    public List<Oferta> findAll() {
        String sql = "SELECT o.*, e.nombre AS empresa_nombre " +
                "FROM OFERTA o " +
                "INNER JOIN EMPRESA e ON o.id_empresa = e.id " +
                "ORDER BY o.fechaPostulacion DESC";
        List<Oferta> listado = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                listado.add(mapRow(rs));
            }
            return listado;
        } catch (SQLException e) {
            throw new DatabaseException("Error al recuperar listado de ofertas", e);
        }
    }

    @Override
    public void update(Oferta entity) {
        String sql = "UPDATE OFERTA SET id_empresa = ?, titulo = ?, descripcion = ?, estado = ?, " +
                "fechaPostulacion = ?, enlace = ?, salario = ?, notas = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, entity.getIdEmpresa());
            pstmt.setString(2, entity.getTitulo());
            pstmt.setString(3, entity.getDescripcion());
            pstmt.setString(4, entity.getEstado());
            pstmt.setDate(5, Date.valueOf(entity.getFechaPostulacion()));
            pstmt.setString(6, entity.getEnlace());
            pstmt.setBigDecimal(7, entity.getSalario());
            pstmt.setString(8, entity.getNotas());
            pstmt.setInt(9, entity.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error al actualizar oferta con ID: " + entity.getId(), e);
        }
    }

    @Override
    public void delete(Integer id) {
        String sql = "DELETE FROM OFERTA WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error al eliminar la oferta con ID: " + id, e);
        }
    }

    @Override
    public List<Oferta> findByEstado(String estado) {
        String sql = "SELECT o.*, e.nombre AS empresa_nombre " +
                "FROM OFERTA o " +
                "INNER JOIN EMPRESA e ON o.id_empresa = e.id " +
                "WHERE o.estado = ? ORDER BY o.fechaPostulacion DESC";
        List<Oferta> listado = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, estado);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    listado.add(mapRow(rs));
                }
            }
            return listado;
        } catch (SQLException e) {
            throw new DatabaseException("Error al filtrar ofertas por estado: " + estado, e);
        }
    }

    @Override
    public List<Oferta> findByEmpresaId(Integer idEmpresa) {
        String sql = "SELECT o.*, e.nombre AS empresa_nombre " +
                "FROM OFERTA o " +
                "INNER JOIN EMPRESA e ON o.id_empresa = e.id " +
                "WHERE o.id_empresa = ? ORDER BY o.fechaPostulacion DESC";
        List<Oferta> listado = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idEmpresa);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    listado.add(mapRow(rs));
                }
            }
            return listado;
        } catch (SQLException e) {
            throw new DatabaseException("Error al filtrar ofertas por empresa ID: " + idEmpresa, e);
        }
    }

    @Override
    public void updateEstado(Integer idOferta, String nuevoEstado) {
        String sql = "UPDATE OFERTA SET estado = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idOferta);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Error al modificar estado de la oferta ID: " + idOferta, e);
        }
    }

    @Override
    public List<Oferta> findByFiltros(String estado, String nombreEmpresa, BigDecimal salarioMin, LocalDate fechaDesde) {
        StringBuilder sql = new StringBuilder(
                "SELECT o.*, e.nombre AS empresa_nombre " +
                        "FROM OFERTA o " +
                        "INNER JOIN EMPRESA e ON o.id_empresa = e.id " +
                        "WHERE 1=1 "
        );

        List<Object> parametros = new ArrayList<>();

        if (estado != null && !estado.isBlank()) {
            sql.append("AND o.estado = ? ");
            parametros.add(estado);
        }
        if (nombreEmpresa != null && !nombreEmpresa.isBlank()) {
            sql.append("AND e.nombre LIKE ? ");
            parametros.add("%" + nombreEmpresa + "%");
        }
        if (salarioMin != null) {
            sql.append("AND o.salario >= ? ");
            parametros.add(salarioMin);
        }
        if (fechaDesde != null) {
            sql.append("AND o.fechaPostulacion >= ? ");
            parametros.add(Date.valueOf(fechaDesde));
        }

        sql.append("ORDER BY o.fechaPostulacion DESC");

        List<Oferta> resultados = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                pstmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resultados.add(mapRow(rs));
                }
            }
            return resultados;
        } catch (SQLException e) {
            throw new DatabaseException("Error en consulta multicriterio de ofertas", e);
        }
    }

    @Override
    public void guardarOfertaConTransaccion(Connection conn, Oferta oferta) throws SQLException {
        String sql = "INSERT INTO OFERTA (id_empresa, titulo, descripcion, estado, fechaPostulacion, enlace, salario, notas) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, oferta.getIdEmpresa());
            pstmt.setString(2, oferta.getTitulo());
            pstmt.setString(3, oferta.getDescripcion());
            pstmt.setString(4, oferta.getEstado());
            pstmt.setDate(5, Date.valueOf(oferta.getFechaPostulacion()));
            pstmt.setString(6, oferta.getEnlace());
            pstmt.setBigDecimal(7, oferta.getSalario());
            pstmt.setString(8, oferta.getNotas());

            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    oferta.setId(keys.getInt(1));
                }
            }
        }
    }
}