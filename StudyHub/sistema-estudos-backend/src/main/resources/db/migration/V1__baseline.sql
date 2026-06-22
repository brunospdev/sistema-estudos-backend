CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL
);

CREATE TABLE disciplinas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    data_criacao TIMESTAMP NOT NULL,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE INDEX idx_disciplinas_usuario_id ON disciplinas(usuario_id);

CREATE TABLE assuntos (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    status BOOLEAN NOT NULL DEFAULT FALSE,
    data_programada DATE,
    data_conclusao DATE,
    disciplina_id BIGINT NOT NULL REFERENCES disciplinas(id) ON DELETE CASCADE
);

CREATE INDEX idx_assuntos_disciplina_id ON assuntos(disciplina_id);

CREATE TABLE registros_estudo_diario (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    data DATE NOT NULL,
    sessoes INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_registro_usuario_data UNIQUE (usuario_id, data)
);

CREATE TABLE configuracoes_pomodoro (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL UNIQUE REFERENCES usuarios(id) ON DELETE CASCADE,
    minutos_foco INT NOT NULL DEFAULT 25,
    minutos_pausa_curta INT NOT NULL DEFAULT 5,
    minutos_pausa_longa INT NOT NULL DEFAULT 15
);
