CREATE TYPE comp_type AS ENUM ('ALL_OR_NOTHING','X_ATTEMPTS');
CREATE TYPE gender AS ENUM ('MAN', 'WOMAN', 'OTHER');
CREATE TYPE comp_section AS ENUM ('OPEN', 'AMATEUR');

ALTER TABLE "user" ADD COLUMN gender gender;

CREATE TABLE IF NOT EXISTS competition (
    id SERIAL PRIMARY KEY,
    gym_id INT NOT NULL REFERENCES gym(id),
    "name" VARCHAR(255) NOT NULL,
    "type" comp_type NOT NULL,
    grace_minutes INT NOT NULL DEFAULT 5 CHECK (grace_minutes >= 0),
    base_points INT,
    attempt_penalty INT,
    min_points INT,
    date_from TIMESTAMP NOT NULL,
    date_to TIMESTAMP NOT NULL,
    created_date TIMESTAMP NOT NULL,
    UNIQUE (id, gym_id)
);

ALTER TABLE competition
    ADD CONSTRAINT chk_competition_scoring CHECK (
        ("type" = 'X_ATTEMPTS'
            AND base_points IS NOT NULL AND base_points >= 0
            AND attempt_penalty IS NOT NULL AND attempt_penalty >= 0
            AND min_points IS NOT NULL AND min_points >= 0 AND min_points <= base_points)
            OR
        ("type" = 'ALL_OR_NOTHING'
            AND base_points IS NULL AND attempt_penalty IS NULL AND min_points IS NULL)
        );

ALTER TABLE boulder ADD COLUMN competition_id INTEGER;

ALTER TABLE boulder ADD COLUMN points INT CHECK (points >= 0);

CREATE INDEX idx_boulder_competition ON boulder(competition_id);

ALTER TABLE boulder ADD CONSTRAINT fk_boulder_competition FOREIGN KEY (competition_id, gym_id) REFERENCES competition(id, gym_id);

ALTER TABLE boulder ADD UNIQUE (id, competition_id);

CREATE TABLE IF NOT EXISTS competition_participant (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES "user"(id),
    competition_id INT NOT NULL REFERENCES competition(id),
    "section" comp_section NOT NULL DEFAULT 'OPEN',
    gender gender NOT NULL,
    last_snapshot INT NOT NULL DEFAULT 0,
    registered_at TIMESTAMP NOT NULL,
    UNIQUE (competition_id, user_id),
    UNIQUE (id, competition_id)
);

CREATE TABLE IF NOT EXISTS competition_result (
    id SERIAL PRIMARY KEY,
    participant_id INT NOT NULL,
    boulder_id INT NOT NULL,
    competition_id INT NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    topped BOOLEAN NOT NULL DEFAULT FALSE,
    points INT NOT NULL DEFAULT 0,
    UNIQUE (participant_id, boulder_id),
    CHECK (NOT topped OR attempts >= 1),
    FOREIGN KEY (participant_id, competition_id)
        REFERENCES competition_participant (id, competition_id),
    FOREIGN KEY (boulder_id, competition_id)
        REFERENCES boulder (id, competition_id)
);

CREATE INDEX idx_result_competition ON competition_result(competition_id);

CREATE TABLE IF NOT EXISTS competition_ranking (
    id SERIAL PRIMARY KEY,
    competition_id INT NOT NULL REFERENCES competition(id),
    competition VARCHAR(255) NOT NULL,
    user_id INT NOT NULL,
    "user" VARCHAR(255),
    gender gender NOT NULL,
    "section" comp_section NOT NULL,
    "rank" INT NOT NULL,
    points INT NOT NULL,
    flashes INT NOT NULL,
    total_attempts INT NOT NULL,
    UNIQUE (competition_id, user_id)
);

CREATE INDEX idx_ranking_lookup ON competition_ranking (competition_id, "section", gender);

CREATE INDEX idx_ranking_comp ON competition_ranking (competition_id);