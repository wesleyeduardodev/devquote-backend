-- Script para inserir usuário ADMIN padrão
-- Este script executa automaticamente na inicialização do Spring Boot

-- Inserir usuário admin padrão (apenas se não existir)
INSERT INTO users (username, password, email, name, active, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at)
SELECT 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXKxXBRFU2PkxKP9BjBU3nQx7zO', 'admin@devquote.com', 'Administrador', true, true, true, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- A senha padrão é: admin123
-- Hash gerado com BCrypt: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXKxXBRFU2PkxKP9BjBU3nQx7zO

-- Inserir perfil ADMIN (o PermissionService já faz isso, mas garantindo)
INSERT INTO profiles (code, name, description, level, active, created_at, updated_at)
SELECT 'ADMIN', 'Administrador', 'Acesso total ao sistema', 1, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM profiles WHERE code = 'ADMIN');

-- Vincular usuário admin ao perfil ADMIN
INSERT INTO user_profiles (user_id, profile_id, active, created_at, updated_at)
SELECT u.id, p.id, true, NOW(), NOW()
FROM users u, profiles p
WHERE u.username = 'admin' 
  AND p.code = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM user_profiles up 
    WHERE up.user_id = u.id AND up.profile_id = p.id
  );