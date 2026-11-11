package com.dam.jobtrack.config;

import com.dam.jobtrack.exception.DatabaseException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {

    private static final HikariDataSource dataSource;

    static {
        Properties props = new Properties();
        try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (is == null) {
                throw new DatabaseException("No se encontró el archivo config.properties en resources");
            }
            props.load(is);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));

            int poolSize = Integer.parseInt(props.getProperty("db.pool.size", "10"));
            config.setMaximumPoolSize(poolSize);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);

            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new DatabaseException("Error cargando la configuración de la base de datos", e);
        }
    }

    private DatabaseConfig() {
        // Constructor privado para clase utilitaria
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static HikariDataSource getDataSource() {
        return dataSource;
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}