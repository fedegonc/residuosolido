-- Script para diagnosticar y arreglar el problema de profileCompleted

-- 1. Ver estructura de la tabla users
\d users;

-- 2. Ver todas las organizaciones y su estado de profileCompleted
SELECT 
    id,
    username,
    email,
    role,
    profile_completed,
    created_at
FROM users 
WHERE role = 'ORGANIZATION'
ORDER BY created_at DESC;

-- 3. Ver el tipo de dato de la columna profile_completed
SELECT 
    column_name, 
    data_type, 
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'users' 
AND column_name = 'profile_completed';

-- 4. SOLUCIÓN TEMPORAL: Forzar manualmente el profileCompleted a true
-- Reemplaza 'TU_USERNAME_AQUI' con el username de tu organización
UPDATE users 
SET profile_completed = true 
WHERE username = 'TU_USERNAME_AQUI' 
AND role = 'ORGANIZATION';

-- 5. Verificar después del UPDATE
SELECT id, username, role, profile_completed 
FROM users 
WHERE username = 'TU_USERNAME_AQUI';

-- 6. Si el problema persiste, verificar si hay triggers o constraints
SELECT 
    trigger_name,
    event_manipulation,
    event_object_table,
    action_statement
FROM information_schema.triggers
WHERE event_object_table = 'users';
