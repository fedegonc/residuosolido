-- Script para recrear la tabla feedback
-- Ejecutar en PostgreSQL

-- Eliminar la tabla existente
DROP TABLE IF EXISTS feedback;

-- Crear la nueva tabla
CREATE TABLE feedback (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_user FOREIGN KEY (user_id) REFERENCES users(id)
);
