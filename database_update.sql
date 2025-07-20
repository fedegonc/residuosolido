-- Script para actualizar la tabla feedback
-- Ejecutar en PostgreSQL

-- Eliminar las columnas que ya no se usan
ALTER TABLE feedback DROP COLUMN IF EXISTS name;
ALTER TABLE feedback DROP COLUMN IF EXISTS email;

-- Agregar la columna user_id si no existe
ALTER TABLE feedback ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- Agregar la constraint de foreign key si no existe
ALTER TABLE feedback ADD CONSTRAINT IF NOT EXISTS fk_feedback_user 
    FOREIGN KEY (user_id) REFERENCES users(id);

-- Hacer que user_id sea NOT NULL (después de agregar datos si es necesario)
-- ALTER TABLE feedback ALTER COLUMN user_id SET NOT NULL;
