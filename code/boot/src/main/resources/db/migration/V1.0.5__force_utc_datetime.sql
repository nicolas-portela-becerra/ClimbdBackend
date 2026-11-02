-- Everything is stored as UTC from now on.
UPDATE "user"     SET created_date  = (created_date  AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC',
                      updated_date  = (updated_date  AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC';
UPDATE gym        SET created_date  = (created_date  AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC',
                      updated_date  = (updated_date  AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC';
UPDATE gym_owner  SET assigned_at   = (assigned_at   AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC';
UPDATE wall_image SET uploaded_date = (uploaded_date AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC';
UPDATE boulder    SET created_date  = (created_date  AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC',
                      updated_date  = (updated_date  AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC';
UPDATE "hold"     SET created_date  = (created_date  AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC',
                      updated_date  = (updated_date  AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC';
UPDATE competition SET date_from    = (date_from     AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC',
                       date_to      = (date_to       AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC',
                       created_date = (created_date  AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC';
UPDATE competition_participant
                  SET registered_at = (registered_at AT TIME ZONE 'Europe/Madrid') AT TIME ZONE 'UTC';
