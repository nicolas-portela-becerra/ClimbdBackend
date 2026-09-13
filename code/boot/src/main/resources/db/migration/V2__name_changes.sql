ALTER TABLE gym RENAME COLUMN created_by_admin_id TO creator_id;

ALTER TABLE wall_image DROP COLUMN is_actual;

ALTER TABLE wall_image DROP COLUMN min_dimension_px;

