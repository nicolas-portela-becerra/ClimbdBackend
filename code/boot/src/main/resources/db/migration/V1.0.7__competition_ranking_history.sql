DROP TABLE IF EXISTS competition_ranking;

CREATE TABLE competition_ranking (
    id SERIAL PRIMARY KEY,
    competition_id INT NOT NULL,
    gym_id INT NOT NULL,
    competition_name VARCHAR(255) NOT NULL,
    date_to TIMESTAMPTZ NOT NULL,
    user_id INT NOT NULL,
    user_name VARCHAR(255),
    "section" comp_section NOT NULL,
    gender gender NOT NULL,
    total_points INT NOT NULL,
    total_attempts INT NOT NULL,
    flash_tops INT NOT NULL,
    UNIQUE (competition_id, user_id)
);
