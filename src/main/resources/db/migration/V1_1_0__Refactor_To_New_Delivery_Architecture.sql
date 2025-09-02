-- Migração para nova arquitetura: Task (1:1) Delivery (1:N) DeliveryItem
-- Esta migração preserva todos os dados existentes

-- 1. Criar tabela delivery_item com dados das entregas atuais
CREATE TABLE delivery_item (
    id BIGSERIAL PRIMARY KEY,
    delivery_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    branch VARCHAR(200),
    source_branch VARCHAR(200),
    pull_request VARCHAR(500),
    script TEXT,
    notes VARCHAR(1000),
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    CONSTRAINT fk_delivery_item_project FOREIGN KEY (project_id) REFERENCES project(id),
    CONSTRAINT fk_delivery_item_created_by FOREIGN KEY (created_by) REFERENCES "user"(id),
    CONSTRAINT fk_delivery_item_updated_by FOREIGN KEY (updated_by) REFERENCES "user"(id)
);

-- 2. Criar índices para performance
CREATE INDEX idx_delivery_item_delivery_id ON delivery_item(delivery_id);
CREATE INDEX idx_delivery_item_project_id ON delivery_item(project_id);
CREATE INDEX idx_delivery_item_status ON delivery_item(status);

-- 3. Criar tabela temporária para mapear entregas únicas por tarefa
CREATE TEMP TABLE delivery_mapping AS
WITH ranked_deliveries AS (
    SELECT 
        d.*,
        ROW_NUMBER() OVER (PARTITION BY d.task_id ORDER BY d.created_at ASC) as rn
    FROM delivery d
),
unique_deliveries AS (
    SELECT 
        rd.task_id,
        rd.id as original_delivery_id,
        CASE 
            WHEN COUNT(*) OVER (PARTITION BY rd.task_id) = 1 THEN rd.status
            ELSE 'PENDING'
        END as new_delivery_status
    FROM ranked_deliveries rd
    WHERE rd.rn = 1
)
SELECT 
    ud.task_id,
    ud.original_delivery_id,
    ud.new_delivery_status
FROM unique_deliveries ud;

-- 4. Criar novas deliveries (uma por tarefa)
INSERT INTO delivery (task_id, status, created_at, updated_at, created_by, updated_by)
SELECT DISTINCT
    d.task_id,
    'PENDING',
    MIN(d.created_at),
    MAX(d.updated_at),
    MIN(d.created_by),
    MAX(d.updated_by)
FROM delivery d
GROUP BY d.task_id;

-- 5. Migrar dados das entregas antigas para delivery_items
INSERT INTO delivery_item (
    delivery_id, project_id, status, branch, source_branch, pull_request,
    script, notes, started_at, finished_at, created_at, updated_at,
    created_by, updated_by
)
SELECT 
    new_d.id as delivery_id,
    old_d.project_id,
    old_d.status,
    old_d.branch,
    old_d.source_branch,
    old_d.pull_request,
    old_d.script,
    old_d.notes,
    old_d.started_at::timestamp,
    old_d.finished_at::timestamp,
    old_d.created_at,
    old_d.updated_at,
    old_d.created_by,
    old_d.updated_by
FROM delivery old_d
JOIN delivery new_d ON new_d.task_id = old_d.task_id
-- Evitar duplicar as novas deliveries que já foram inseridas
WHERE old_d.id NOT IN (
    SELECT DISTINCT new_d2.id FROM delivery new_d2 WHERE new_d2.task_id = old_d.task_id AND new_d2.id = old_d.id
);

-- 6. Adicionar constraint de foreign key para delivery_item -> delivery
ALTER TABLE delivery_item 
ADD CONSTRAINT fk_delivery_item_delivery 
FOREIGN KEY (delivery_id) REFERENCES delivery(id) ON DELETE CASCADE;

-- 7. Remover campos antigos da tabela delivery
ALTER TABLE delivery DROP COLUMN IF EXISTS project_id;
ALTER TABLE delivery DROP COLUMN IF EXISTS branch;
ALTER TABLE delivery DROP COLUMN IF EXISTS source_branch;
ALTER TABLE delivery DROP COLUMN IF EXISTS pull_request;
ALTER TABLE delivery DROP COLUMN IF EXISTS script;
ALTER TABLE delivery DROP COLUMN IF EXISTS notes;
ALTER TABLE delivery DROP COLUMN IF EXISTS started_at;
ALTER TABLE delivery DROP COLUMN IF EXISTS finished_at;

-- 8. Adicionar constraint unique para task_id (relacionamento 1:1)
ALTER TABLE delivery ADD CONSTRAINT uk_delivery_task_id UNIQUE (task_id);

-- 9. Remover deliveries antigas duplicadas (manter apenas as novas)
DELETE FROM delivery 
WHERE id NOT IN (
    SELECT MAX(id) 
    FROM delivery 
    GROUP BY task_id
);

-- 10. Atualizar status das deliveries baseado nos itens
UPDATE delivery 
SET status = (
    CASE 
        WHEN EXISTS(SELECT 1 FROM delivery_item di WHERE di.delivery_id = delivery.id AND di.status = 'PRODUCTION') THEN 'PRODUCTION'
        WHEN EXISTS(SELECT 1 FROM delivery_item di WHERE di.delivery_id = delivery.id AND di.status = 'APPROVED') THEN 'APPROVED'
        WHEN EXISTS(SELECT 1 FROM delivery_item di WHERE di.delivery_id = delivery.id AND di.status = 'DELIVERED') THEN 'DELIVERED'
        WHEN EXISTS(SELECT 1 FROM delivery_item di WHERE di.delivery_id = delivery.id AND di.status = 'HOMOLOGATION') THEN 'HOMOLOGATION'
        WHEN EXISTS(SELECT 1 FROM delivery_item di WHERE di.delivery_id = delivery.id AND di.status = 'DEVELOPMENT') THEN 'DEVELOPMENT'
        WHEN EXISTS(SELECT 1 FROM delivery_item di WHERE di.delivery_id = delivery.id AND di.status = 'REJECTED') THEN 'REJECTED'
        ELSE 'PENDING'
    END
);

-- 11. Adicionar comentários de documentação
COMMENT ON TABLE delivery IS 'Entrega única por tarefa - relacionamento 1:1 com Task';
COMMENT ON TABLE delivery_item IS 'Itens específicos da entrega por projeto - relacionamento N:1 com Delivery';
COMMENT ON COLUMN delivery.status IS 'Status calculado automaticamente baseado nos itens da entrega';
COMMENT ON COLUMN delivery_item.status IS 'Status específico do item por projeto';