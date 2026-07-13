CREATE DATABASE IF NOT EXISTS chess_game;
USE chess_game;

CREATE TABLE IF NOT EXISTS usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombreUsuario VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    fechaRegistro DATETIME DEFAULT NOW(),
    partidasGanadas INT DEFAULT 0,
    partidasPerdidas INT DEFAULT 0,
    partidasEmpatadas INT DEFAULT 0,
    esAdmin BOOLEAN DEFAULT FALSE,
    ultimo_acceso TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO usuario (nombreUsuario, nombre, apellido, password, esAdmin) 
SELECT 'gfarfan', 'Gustavo', 'Farfan', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', TRUE
WHERE NOT EXISTS (SELECT * FROM usuario WHERE nombreUsuario = 'gfarfan');

CREATE TABLE IF NOT EXISTS partida_online (
    id VARCHAR(50) PRIMARY KEY,
    jugador_blancas VARCHAR(50) NOT NULL,
    jugador_negras VARCHAR(50) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    movimientos TEXT,
    turno VARCHAR(10) NOT NULL,
    ultimo_cambio TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
