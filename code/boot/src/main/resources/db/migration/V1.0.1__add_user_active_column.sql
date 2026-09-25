ALTER TABLE "user" ADD COLUMN active BOOLEAN NOT NULL DEFAULT FALSE;

-- Existing accounts stay active; only new registrations require admin activation
UPDATE "user" SET active = TRUE;
