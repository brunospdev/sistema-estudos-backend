ALTER TABLE disciplinas
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0,
    ADD COLUMN oculta BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE assuntos
    ADD COLUMN status_estudo VARCHAR(20) NOT NULL DEFAULT 'NAO_INICIADO',
    ADD COLUMN sort_order INT NOT NULL DEFAULT 0,
    ADD COLUMN horas_acumuladas DECIMAL(8, 2) NOT NULL DEFAULT 0,
    ADD COLUMN ultima_sessao_em TIMESTAMP;

UPDATE assuntos
SET status_estudo = CASE WHEN status = TRUE THEN 'DOMINADO' ELSE 'NAO_INICIADO' END;

CREATE TABLE marcos_usuario (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    disciplina_id BIGINT REFERENCES disciplinas(id) ON DELETE SET NULL,
    tipo VARCHAR(20) NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    data DATE NOT NULL,
    notas TEXT,
    eh_principal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_marcos_usuario_data ON marcos_usuario(usuario_id, data);

CREATE TABLE preferencias_usuario (
    usuario_id BIGINT PRIMARY KEY REFERENCES usuarios(id) ON DELETE CASCADE,
    layout_mode VARCHAR(20) NOT NULL DEFAULT 'GRUPO_TOPICO',
    label_grupo VARCHAR(40) NOT NULL DEFAULT 'Matéria',
    label_item VARCHAR(40) NOT NULL DEFAULT 'Tópico',
    meta_horas_diarias DECIMAL(4, 2),
    marco_principal_id BIGINT REFERENCES marcos_usuario(id) ON DELETE SET NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE sessoes_estudo (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    assunto_id BIGINT REFERENCES assuntos(id) ON DELETE SET NULL,
    disciplina_id BIGINT REFERENCES disciplinas(id) ON DELETE SET NULL,
    inicio_em TIMESTAMP NOT NULL,
    fim_em TIMESTAMP NOT NULL,
    duracao_minutos INT NOT NULL,
    dificuldade VARCHAR(10),
    anotacao TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_sessoes_usuario_inicio ON sessoes_estudo(usuario_id, inicio_em);
CREATE INDEX idx_sessoes_assunto ON sessoes_estudo(assunto_id);

CREATE TABLE eventos_status (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    assunto_id BIGINT NOT NULL REFERENCES assuntos(id) ON DELETE CASCADE,
    status_anterior VARCHAR(20),
    status_novo VARCHAR(20) NOT NULL,
    ocorrido_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_eventos_status_assunto ON eventos_status(assunto_id);
