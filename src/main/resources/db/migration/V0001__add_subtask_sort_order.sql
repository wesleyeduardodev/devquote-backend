-- Adiciona campo de ordenacao em sub_task
-- Executado manualmente em prod (projeto nao usa Flyway/Liquibase)
-- Idempotente: pode ser re-executado sem efeito colateral

BEGIN;

ALTER TABLE sub_task ADD COLUMN IF NOT EXISTS sort_order INTEGER;

UPDATE sub_task
SET sort_order = sub.rn
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY task_id ORDER BY id ASC) AS rn
    FROM sub_task
) AS sub
WHERE sub_task.id = sub.id AND sub_task.sort_order IS NULL;

ALTER TABLE sub_task ALTER COLUMN sort_order SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_sub_task_task_sort ON sub_task (task_id, sort_order);

COMMIT;
