CREATE TYPE comp_type AS ENUM ('ALL_OR_NOTHING','X_ATTEMPTS');
CREATE TYPE gender AS ENUM ('MAN', 'WOMAN', 'OTHER');
CREATE TYPE comp_section AS ENUM ('OPEN', 'AMATEUR');

ALTER TABLE "user" ADD COLUMN gender gender;

CREATE TABLE IF NOT EXISTS competition (
    id SERIAL PRIMARY KEY,
    gym_id INT NOT NULL REFERENCES gym(id),
    "name" VARCHAR(255) NOT NULL,
    "type" comp_type NOT NULL,
    max_points INT NOT NULL,
    max_attempts INT NULL,
    date_from TIMESTAMP NOT NULL,
    date_to TIMESTAMP NOT NULL,
    created_date TIMESTAMP NOT NULL,
    UNIQUE (id, gym_id)
);

ALTER TABLE boulder ADD COLUMN competition_id INTEGER;

CREATE INDEX idx_boulder_competition ON boulder(competition_id);

ALTER TABLE boulder ADD CONSTRAINT fk_boulder_competition FOREIGN KEY (competition_id, gym_id) REFERENCES competition(id, gym_id);

ALTER TABLE boulder ADD UNIQUE (id, competition_id);

CREATE TABLE IF NOT EXISTS competition_participant (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES "user"(id),
    competition_id INT NOT NULL REFERENCES competition(id),
    "section" comp_section NOT NULL DEFAULT 'OPEN',
    registered_at TIMESTAMP NOT NULL,
    UNIQUE (competition_id, user_id),
    UNIQUE (id, competition_id)
);

CREATE TABLE IF NOT EXISTS competition_result (
    id SERIAL PRIMARY KEY,
    participant_id INT NOT NULL,
    boulder_id INT NOT NULL,
    competition_id INT NOT NULL,
    attempts INT NULL,
    topped BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (participant_id, boulder_id),
    FOREIGN KEY (participant_id, competition_id)
        REFERENCES competition_participant (id, competition_id),
    FOREIGN KEY (boulder_id, competition_id)
        REFERENCES boulder (id, competition_id)
);
