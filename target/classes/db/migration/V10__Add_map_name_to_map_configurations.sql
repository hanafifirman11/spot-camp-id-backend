ALTER TABLE map_configurations
ADD COLUMN map_name VARCHAR(200);

UPDATE map_configurations
SET map_name = 'Main Map'
WHERE map_name IS NULL OR map_name = '';

ALTER TABLE map_configurations
ALTER COLUMN map_name SET NOT NULL;

DROP INDEX IF EXISTS idx_map_config_active;

CREATE UNIQUE INDEX idx_map_config_active
    ON map_configurations(campsite_id, map_name)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_map_config_campsite_name
    ON map_configurations(campsite_id, map_name);
