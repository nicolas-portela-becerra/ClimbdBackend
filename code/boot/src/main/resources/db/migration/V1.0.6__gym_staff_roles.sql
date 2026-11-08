-- Gym roles are per gym (a user can own one gym and set in another), so they live in a
-- membership table instead of "user".role, which stays the global role (ADMIN | USER).
ALTER TABLE gym_owner RENAME TO gym_staff;

-- Existing rows were owners
ALTER TABLE gym_staff ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'OWNER';
ALTER TABLE gym_staff ALTER COLUMN role DROP DEFAULT;
ALTER TABLE gym_staff ADD CONSTRAINT chk_gym_staff_role CHECK (role IN ('OWNER', 'SETTER'));

ALTER INDEX idx_gym_owner_user_id RENAME TO idx_gym_staff_user_id;
ALTER INDEX idx_gym_owner_gym_id RENAME TO idx_gym_staff_gym_id;

ALTER TABLE "user" ADD CONSTRAINT chk_user_role CHECK (role IN ('ADMIN', 'USER'));
