-- Existing values are UTC (V1.0.5), so read them as UTC explicitly instead of the session zone.
ALTER TABLE boulder
    ALTER COLUMN created_date TYPE TIMESTAMPTZ USING created_date AT TIME ZONE 'UTC',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ USING updated_date AT TIME ZONE 'UTC';

ALTER TABLE competition
    ALTER COLUMN date_from    TYPE TIMESTAMPTZ USING date_from    AT TIME ZONE 'UTC',
    ALTER COLUMN date_to      TYPE TIMESTAMPTZ USING date_to      AT TIME ZONE 'UTC',
    ALTER COLUMN created_date TYPE TIMESTAMPTZ USING created_date AT TIME ZONE 'UTC';

ALTER TABLE competition_participant
    ALTER COLUMN registered_at TYPE TIMESTAMPTZ USING registered_at AT TIME ZONE 'UTC';

ALTER TABLE gym
    ALTER COLUMN created_date TYPE TIMESTAMPTZ USING created_date AT TIME ZONE 'UTC',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ USING updated_date AT TIME ZONE 'UTC';

ALTER TABLE gym_owner
    ALTER COLUMN assigned_at TYPE TIMESTAMPTZ USING assigned_at AT TIME ZONE 'UTC';

ALTER TABLE "hold"
    ALTER COLUMN created_date TYPE TIMESTAMPTZ USING created_date AT TIME ZONE 'UTC',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ USING updated_date AT TIME ZONE 'UTC';

ALTER TABLE "user"
    ALTER COLUMN created_date TYPE TIMESTAMPTZ USING created_date AT TIME ZONE 'UTC',
    ALTER COLUMN updated_date TYPE TIMESTAMPTZ USING updated_date AT TIME ZONE 'UTC';

ALTER TABLE wall_image
    ALTER COLUMN uploaded_date TYPE TIMESTAMPTZ USING uploaded_date AT TIME ZONE 'UTC';

-- Existing gyms get Madrid; the column can only be NOT NULL once they are backfilled.
ALTER TABLE gym ADD COLUMN time_zone VARCHAR(255);
UPDATE gym SET time_zone = 'Europe/Madrid';
ALTER TABLE gym ALTER COLUMN time_zone SET NOT NULL;
