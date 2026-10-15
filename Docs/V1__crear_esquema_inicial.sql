-- WoofMap · Migración inicial (Flyway)
-- Ruta: backend/src/main/resources/db/migration/V1__crear_esquema_inicial.sql

CREATE EXTENSION IF NOT EXISTS postgis;

-- ---------------------------------------------------------------
-- USUARIO
-- ---------------------------------------------------------------
CREATE TABLE usuario (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,          -- BCrypt (RNF01)
    fecha_alta    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_usuario_email UNIQUE (email)
);

-- ---------------------------------------------------------------
-- LUGAR
-- ---------------------------------------------------------------
CREATE TABLE lugar (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre         VARCHAR(150) NOT NULL,
    descripcion    TEXT,
    categoria      VARCHAR(50)  NOT NULL,
    ubicacion      geography(Point, 4326) NOT NULL,  -- lon/lat WGS84
    usuario_id     BIGINT       NOT NULL,
    fecha_creacion TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_lugar_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

-- Índice espacial para búsquedas por proximidad (RNF04)
CREATE INDEX idx_lugar_ubicacion ON lugar USING GIST (ubicacion);
CREATE INDEX idx_lugar_categoria ON lugar (categoria);
CREATE INDEX idx_lugar_usuario   ON lugar (usuario_id);

-- ---------------------------------------------------------------
-- FOTO
-- ---------------------------------------------------------------
CREATE TABLE foto (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url          VARCHAR(500) NOT NULL,           -- ruta/URL, nunca el binario
    lugar_id     BIGINT       NOT NULL,
    usuario_id   BIGINT       NOT NULL,
    fecha_subida TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_foto_lugar
        FOREIGN KEY (lugar_id)   REFERENCES lugar (id)   ON DELETE CASCADE,
    CONSTRAINT fk_foto_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

CREATE INDEX idx_foto_lugar   ON foto (lugar_id);
CREATE INDEX idx_foto_usuario ON foto (usuario_id);

-- ---------------------------------------------------------------
-- COMENTARIO
-- ---------------------------------------------------------------
CREATE TABLE comentario (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    texto      TEXT        NOT NULL,
    valoracion SMALLINT    NOT NULL,
    lugar_id   BIGINT      NOT NULL,
    usuario_id BIGINT      NOT NULL,
    fecha      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_comentario_valoracion CHECK (valoracion BETWEEN 1 AND 5),
    CONSTRAINT fk_comentario_lugar
        FOREIGN KEY (lugar_id)   REFERENCES lugar (id)   ON DELETE CASCADE,
    CONSTRAINT fk_comentario_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

CREATE INDEX idx_comentario_lugar   ON comentario (lugar_id);
CREATE INDEX idx_comentario_usuario ON comentario (usuario_id);
