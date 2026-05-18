-- Adiciona campo de ordenacao em delivery_item e delivery_operational_item
-- Executado manualmente em prod (projeto nao usa Flyway/Liquibase)
-- Idempotente: pode ser re-executado sem efeito colateral

BEGIN;

-- ============ delivery_item (fluxo DESENVOLVIMENTO) ============
ALTER TABLE delivery_item ADD COLUMN IF NOT EXISTS sort_order INTEGER;

UPDATE delivery_item
SET sort_order = sub.rn
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY delivery_id ORDER BY id ASC) AS rn
    FROM delivery_item
) AS sub
WHERE delivery_item.id = sub.id AND delivery_item.sort_order IS NULL;

ALTER TABLE delivery_item ALTER COLUMN sort_order SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_delivery_item_delivery_sort
    ON delivery_item (delivery_id, sort_order);

-- ============ delivery_operational_item (fluxo OPERACIONAL) ============
ALTER TABLE delivery_operational_item ADD COLUMN IF NOT EXISTS sort_order INTEGER;

UPDATE delivery_operational_item
SET sort_order = sub.rn
FROM (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY delivery_id ORDER BY id ASC) AS rn
    FROM delivery_operational_item
) AS sub
WHERE delivery_operational_item.id = sub.id AND delivery_operational_item.sort_order IS NULL;

ALTER TABLE delivery_operational_item ALTER COLUMN sort_order SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_delivery_op_item_delivery_sort
    ON delivery_operational_item (delivery_id, sort_order);

COMMIT;
