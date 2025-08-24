-- Script para remover tabelas do sistema antigo de roles
-- Execute este script no banco de dados antes de subir a aplicação

-- Remove a tabela de relacionamento role_permissions se existir
DROP TABLE IF EXISTS role_permissions CASCADE;

-- Remove a tabela de relacionamento user_roles se existir  
DROP TABLE IF EXISTS user_roles CASCADE;

-- Remove a tabela roles se existir
DROP TABLE IF EXISTS roles CASCADE;

-- Verifica se existem colunas role antigas na tabela users e remove
DO $$
BEGIN
    -- Remove coluna role_id se existir
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='role_id') THEN
        ALTER TABLE users DROP COLUMN role_id;
    END IF;
END$$;