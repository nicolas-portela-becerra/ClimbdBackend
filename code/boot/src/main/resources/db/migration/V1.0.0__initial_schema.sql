CREATE TABLE IF NOT EXISTS "user" (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(255),
    password_hash VARCHAR(255),
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gym (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    description TEXT,
    creator_id INT NOT NULL REFERENCES "user"(id),
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gym_owner (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES "user"(id),
    gym_id INT NOT NULL REFERENCES gym(id),
    assigned_by INT NOT NULL REFERENCES "user"(id),
    assigned_at TIMESTAMP NOT NULL,
    UNIQUE(user_id, gym_id)
);

CREATE TABLE IF NOT EXISTS wall_image (
    id SERIAL PRIMARY KEY,
    gym_id INT NOT NULL REFERENCES gym(id),
    wall_name VARCHAR(255) NOT NULL,
    image_data BYTEA NOT NULL,
    thumbnail BYTEA NOT NULL,
    mime_type VARCHAR(20) NOT NULL,
    width_px INT,
    height_px INT,
    uploaded_by INT NOT NULL REFERENCES "user"(id),
    uploaded_date TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS boulder (
    id SERIAL PRIMARY KEY,
    gym_id INT NOT NULL REFERENCES gym(id),
    wall_image_id INT NOT NULL REFERENCES wall_image(id),
    creator_id INT NOT NULL REFERENCES "user"(id),
    name VARCHAR(255) NOT NULL,
    grade VARCHAR(10) NOT NULL,
    description TEXT,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "hold" (
    id SERIAL PRIMARY KEY,
    boulder_id INT NOT NULL REFERENCES boulder(id),
    x_ratio NUMERIC(5,4) NOT NULL,
    y_ratio NUMERIC(5,4) NOT NULL,
    radius_ratio NUMERIC(5,4) NOT NULL,
    sequence_order INT,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP
);

CREATE INDEX idx_gym_owner_user_id ON gym_owner(user_id);
CREATE INDEX idx_gym_owner_gym_id ON gym_owner(gym_id);
CREATE INDEX idx_wall_image_gym_id ON wall_image(gym_id);
CREATE INDEX idx_wall_image_gym_actual ON wall_image(gym_id, is_actual);
CREATE INDEX idx_boulder_gym_id ON boulder(gym_id);
CREATE INDEX idx_boulder_wall_image_id ON boulder(wall_image_id);
CREATE INDEX idx_boulder_created_date ON boulder(created_date DESC);
CREATE INDEX idx_hold_boulder_id ON "hold"(boulder_id);
