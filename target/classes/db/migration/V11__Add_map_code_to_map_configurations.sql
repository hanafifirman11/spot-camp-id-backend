ALTER TABLE map_configurations
ADD COLUMN map_code VARCHAR(20);

UPDATE map_configurations
SET map_code = CONCAT('MAP-', LPAD(CAST(id AS VARCHAR), 8, '0'))
WHERE map_code IS NULL OR map_code = '';

ALTER TABLE map_configurations
ALTER COLUMN map_code SET NOT NULL;

DROP INDEX IF EXISTS idx_map_config_active;

CREATE UNIQUE INDEX idx_map_config_active
    ON map_configurations(campsite_id, map_code)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_map_config_campsite_code
    ON map_configurations(campsite_id, map_code);
