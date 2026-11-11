package com.dam.jobtrack.service;

import com.dam.jobtrack.exception.DatabaseException;
import com.dam.jobtrack.model.Oferta;
import com.dam.jobtrack.repository.OfertaDAO;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class OfertaService {

    private final OfertaDAO ofertaDAO;
    private final DataSource dataSource;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public OfertaService(OfertaDAO ofertaDAO, DataSource dataSource) {
        this.ofertaDAO = ofertaDAO;
        this.dataSource = dataSource;
    }

    public List<Oferta> obtenerTodas() {
        return ofertaDAO.findAll();
    }

    public Optional<Oferta> obtenerPorId(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El identificador proporcionado no es válido.");
        }
        return ofertaDAO.findById(id);
    }

    public Oferta registrarOferta(Oferta oferta) {
        validarOferta(oferta);
        return ofertaDAO.save(oferta);
    }

    public void actualizarOferta(Oferta oferta) {
        if (oferta.getId() == null) {
            throw new IllegalArgumentException("No se puede actualizar una oferta sin ID.");
        }
        validarOferta(oferta);
        ofertaDAO.update(oferta);
    }

    public void eliminarOferta(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Identificador inválido para borrado.");
        }
        ofertaDAO.delete(id);
    }

    public void cambiarEstado(Integer idOferta, String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new IllegalArgumentException("El nuevo estado no puede estar vacío.");
        }
        ofertaDAO.updateEstado(idOferta, nuevoEstado);
    }

    public List<Oferta> filtrarOfertas(String estado, String empresa, BigDecimal salarioMin, LocalDate fechaDesde) {
        return ofertaDAO.findByFiltros(estado, empresa, salarioMin, fechaDesde);
    }

    /**
     * Importa masivamente postulaciones desde un fichero CSV delimitado por punto y coma (;) o comas (,).
     * Ejecuta toda la inserción dentro de una única transacción ACID: si falla una fila, se cancela todo.
     * Formato esperado por línea: Titulo;NombreEmpresa;Estado;Fecha(dd/MM/yyyy);Salario;Notas
     */
    public int importarDesdeCsv(String rutaCsv) {
        int registrosImportados = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(rutaCsv));
             Connection conn = dataSource.getConnection()) {

            conn.setAutoCommit(false); // Iniciar transacción manual

            try {
                String linea;
                while ((linea = br.readLine()) != null) {
                    if (linea.isBlank() || linea.startsWith("#") || linea.toLowerCase().startsWith("titulo")) {
                        continue; // Omitir líneas vacías, comentarios o cabeceras
                    }

                    String[] campos = linea.contains(";") ? linea.split(";") : linea.split(",");
                    if (campos.length < 4) {
                        continue;
                    }

                    String titulo = campos[0].trim();
                    String nombreEmpresa = campos[1].trim();
                    String estado = campos[2].trim();
                    LocalDate fecha = LocalDate.parse(campos[3].trim(), FORMATO_FECHA);
                    BigDecimal salario = (campos.length > 4 && !campos[4].isBlank())
                            ? new BigDecimal(campos[4].trim())
                            : null;
                    String notas = (campos.length > 5) ? campos[5].trim() : "";

                    // 1. Obtener o insertar la empresa vinculada dentro de la transacción
                    int idEmpresa = obtenerOInsertarEmpresaTransaccional(conn, nombreEmpresa);

                    // 2. Registrar la oferta asociada
                    Oferta nuevaOferta = new Oferta(null, idEmpresa, titulo, "", estado, fecha, "", salario, notas);
                    ofertaDAO.guardarOfertaConTransaccion(conn, nuevaOferta);

                    registrosImportados++;
                }

                conn.commit(); // Confirmar transacción completa
                return registrosImportados;

            } catch (Exception e) {
                conn.rollback(); // Rollback total ante cualquier inconsistencia
                throw new DatabaseException("Error durante la importación CSV. Transacción revertida.", e);
            }

        } catch (IOException | SQLException e) {
            throw new DatabaseException("Error al procesar el archivo CSV de importación", e);
        }
    }

    private int obtenerOInsertarEmpresaTransaccional(Connection conn, String nombreEmpresa) throws SQLException {
        String sqlBuscar = "SELECT id FROM EMPRESA WHERE nombre = ?";
        try (PreparedStatement psBuscar = conn.prepareStatement(sqlBuscar)) {
            psBuscar.setString(1, nombreEmpresa);
            try (ResultSet rs = psBuscar.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        // Si no existe, se inserta una básica
        String sqlInsertar = "INSERT INTO EMPRESA (nombre, sector, web, fechaRegistro) VALUES (?, 'Tecnología', '', ?)";
        try (PreparedStatement psInsertar = conn.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
            psInsertar.setString(1, nombreEmpresa);
            psInsertar.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            psInsertar.executeUpdate();

            try (ResultSet keys = psInsertar.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("No se pudo obtener ni generar el ID para la empresa: " + nombreEmpresa);
    }

    private void validarOferta(Oferta oferta) {
        if (oferta == null) {
            throw new IllegalArgumentException("La oferta no puede ser nula.");
        }
        if (oferta.getTitulo() == null || oferta.getTitulo().isBlank()) {
            throw new IllegalArgumentException("El título del puesto es obligatorio.");
        }
        if (oferta.getIdEmpresa() == null || oferta.getIdEmpresa() <= 0) {
            throw new IllegalArgumentException("La oferta debe tener asignada una empresa válida.");
        }
        if (oferta.getEstado() == null || oferta.getEstado().isBlank()) {
            throw new IllegalArgumentException("El estado de la postulación es obligatorio.");
        }
        if (oferta.getFechaPostulacion() == null) {
            throw new IllegalArgumentException("La fecha de postulación es obligatoria.");
        }
        if (oferta.getSalario() != null && oferta.getSalario().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El salario no puede ser un valor negativo.");
        }
    }
}