CREATE TABLE recorrencias (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT       NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    frequencia      VARCHAR(20)  NOT NULL,
    intervalo       INT          NOT NULL DEFAULT 1,
    dias_semana     VARCHAR(30),
    dia_mes         INT,
    data_inicio     DATE         NOT NULL,
    data_fim        DATE,
    max_ocorrencias INT,
    ativa           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

ALTER TABLE assuntos
    ADD COLUMN recorrencia_id BIGINT REFERENCES recorrencias(id) ON DELETE SET NULL,
    ADD COLUMN indice_ocorrencia INT;

CREATE INDEX idx_assuntos_recorrencia ON assuntos(recorrencia_id);
CREATE INDEX idx_recorrencias_usuario ON recorrencias(usuario_id);
