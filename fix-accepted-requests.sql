-- Script para corregir solicitudes que fueron aceptadas con el estado ACCEPTED
-- y cambiarlas a IN_PROGRESS para que aparezcan en el dashboard

-- Ver las solicitudes que tienen estado ACCEPTED
SELECT id, description, status, organization_id 
FROM requests 
WHERE status = 'ACCEPTED';

-- Actualizar solicitudes ACCEPTED a IN_PROGRESS
UPDATE requests 
SET status = 'IN_PROGRESS' 
WHERE status = 'ACCEPTED';

-- Verificar el cambio
SELECT id, description, status, organization_id 
FROM requests 
WHERE status = 'IN_PROGRESS';
