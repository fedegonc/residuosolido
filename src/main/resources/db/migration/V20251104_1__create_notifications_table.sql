-- Crear tabla de notificaciones
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(50) NOT NULL,
    user_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    read_at TIMESTAMP,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    action_url VARCHAR(255),
    
    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Índices para mejorar rendimiento
CREATE INDEX idx_notification_user ON notifications(user_id);
CREATE INDEX idx_notification_created_at ON notifications(created_at);
CREATE INDEX idx_notification_is_read ON notifications(is_read);
