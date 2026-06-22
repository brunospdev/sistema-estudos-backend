ALTER TABLE disciplinas
    ADD COLUMN parent_id BIGINT REFERENCES disciplinas(id) ON DELETE CASCADE;

CREATE INDEX idx_disciplinas_parent_id ON disciplinas(parent_id);

ALTER TABLE assuntos
    ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'CONTEUDO',
    ADD COLUMN parent_item_id BIGINT REFERENCES assuntos(id) ON DELETE CASCADE,
    ADD COLUMN data_entrega DATE,
    ADD COLUMN data_realizada DATE,
    ADD COLUMN nota DECIMAL(8,2),
    ADD COLUMN nota_maxima DECIMAL(8,2),
    ADD COLUMN entrega_concluida BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_assuntos_parent_item ON assuntos(parent_item_id);
CREATE INDEX idx_assuntos_tipo ON assuntos(tipo);

ALTER TABLE preferencias_usuario
    ADD COLUMN profundidade_grupos INT NOT NULL DEFAULT 1,
    ADD COLUMN label_grupo_nivel2 VARCHAR(40),
    ADD COLUMN preset_persona VARCHAR(40);
