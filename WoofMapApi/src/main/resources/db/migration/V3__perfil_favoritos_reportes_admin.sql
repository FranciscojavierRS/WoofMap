-- ---------------------------------------------------------------
-- ROL ADMINISTRADOR
-- ---------------------------------------------------------------
ALTER TABLE usuario
    ADD COLUMN administrador BOOLEAN NOT NULL DEFAULT false;

-- Promociona manualmente al primer administrador:
--   UPDATE usuario SET administrador = true WHERE email = 'admin@woofmap.com';

-- ---------------------------------------------------------------
-- FAVORITOS
-- ---------------------------------------------------------------
CREATE TABLE favorito (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id BIGINT      NOT NULL,
    lugar_id   BIGINT      NOT NULL,
    fecha      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_favorito_usuario_lugar UNIQUE (usuario_id, lugar_id),
    CONSTRAINT fk_favorito_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT fk_favorito_lugar
        FOREIGN KEY (lugar_id) REFERENCES lugar (id) ON DELETE CASCADE
);

CREATE INDEX idx_favorito_usuario ON favorito (usuario_id);

-- ---------------------------------------------------------------
-- REPORTES DE CONTENIDO
-- ---------------------------------------------------------------
CREATE TABLE reporte (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id    BIGINT      NOT NULL,
    tipo          VARCHAR(20) NOT NULL,
    referencia_id BIGINT      NOT NULL,
    motivo        TEXT        NOT NULL,
    resuelto      BOOLEAN     NOT NULL DEFAULT false,
    fecha         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_reporte_tipo CHECK (tipo IN ('LUGAR', 'FOTO', 'COMENTARIO')),
    CONSTRAINT fk_reporte_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE INDEX idx_reporte_resuelto ON reporte (resuelto);
