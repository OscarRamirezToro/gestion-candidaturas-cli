CREATE DATABASE IF NOT EXISTS jobtrack_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jobtrack_db;

-- Tabla EMPRESA
CREATE TABLE IF NOT EXISTS EMPRESA (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       nombre VARCHAR(150) NOT NULL UNIQUE,
    sector VARCHAR(100),
    web VARCHAR(255),
    fechaRegistro DATE NOT NULL
    );

-- Tabla OFERTA (Candidatura)
CREATE TABLE IF NOT EXISTS OFERTA (
                                      id INT AUTO_INCREMENT PRIMARY KEY,
                                      id_empresa INT NOT NULL,
                                      titulo VARCHAR(150) NOT NULL,
    descripcion TEXT,
    estado VARCHAR(50) NOT NULL,
    fechaPostulacion DATE NOT NULL,
    enlace VARCHAR(255),
    salario DECIMAL(10, 2),
    notas TEXT,
    FOREIGN KEY (id_empresa) REFERENCES EMPRESA(id) ON DELETE CASCADE
    );

-- Tabla ENTREVISTA
CREATE TABLE IF NOT EXISTS ENTREVISTA (
                                          id INT AUTO_INCREMENT PRIMARY KEY,
                                          id_oferta INT NOT NULL,
                                          fecha DATE NOT NULL,
                                          tipo VARCHAR(50) NOT NULL,
    notas TEXT,
    resultado VARCHAR(50),
    FOREIGN KEY (id_oferta) REFERENCES OFERTA(id) ON DELETE CASCADE
    );