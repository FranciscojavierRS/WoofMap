-- ---------------------------------------------------------------
-- PERRO (concurso Perro del Mes/Semana)
-- ---------------------------------------------------------------
CREATE TABLE perro (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    url           VARCHAR(500) NOT NULL,
    usuario_id    BIGINT       NOT NULL,
    fecha_subida  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_perro_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

CREATE INDEX idx_perro_usuario ON perro (usuario_id);

-- ---------------------------------------------------------------
-- VOTO_PERRO
-- ---------------------------------------------------------------
CREATE TABLE voto_perro (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    perro_id   BIGINT      NOT NULL,
    usuario_id BIGINT      NOT NULL,
    positivo   BOOLEAN     NOT NULL,
    fecha      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_voto_perro_usuario UNIQUE (perro_id, usuario_id),
    CONSTRAINT fk_voto_perro
        FOREIGN KEY (perro_id) REFERENCES perro (id) ON DELETE CASCADE,
    CONSTRAINT fk_voto_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

CREATE INDEX idx_voto_perro_perro   ON voto_perro (perro_id);
CREATE INDEX idx_voto_perro_usuario ON voto_perro (usuario_id);
CREATE INDEX idx_voto_perro_fecha   ON voto_perro (fecha);
