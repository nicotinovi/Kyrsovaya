CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN ('VISITOR', 'ADMINISTRATOR')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(30) NOT NULL CHECK (event_type IN ('TOURNAMENT', 'TRAINING')),
    title VARCHAR(200) NOT NULL,
    description TEXT,
    date_time TIMESTAMP NOT NULL,
    max_participants INTEGER NOT NULL CHECK (max_participants > 0),
    status VARCHAR(40) NOT NULL CHECK (status IN ('OPEN_FOR_REGISTRATION', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    visitor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL CHECK (status IN ('REGISTERED', 'PRESENT', 'CANCELLED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_enrollment_visitor_event UNIQUE (visitor_id, event_id)
);

CREATE TABLE player_ratings (
    id BIGSERIAL PRIMARY KEY,
    visitor_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    total_points INTEGER NOT NULL DEFAULT 0,
    rank_position INTEGER,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rating_entries (
    id BIGSERIAL PRIMARY KEY,
    rating_id BIGINT NOT NULL REFERENCES player_ratings(id) ON DELETE CASCADE,
    visitor_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tournament_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    points INTEGER NOT NULL CHECK (points >= 0),
    assigned_by_admin_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_rating_entry_visitor_tournament UNIQUE (visitor_id, tournament_id)
);

CREATE INDEX idx_events_date_time ON events(date_time);
CREATE INDEX idx_events_status ON events(status);
CREATE INDEX idx_events_type ON events(event_type);
CREATE INDEX idx_enrollments_visitor_id ON enrollments(visitor_id);
CREATE INDEX idx_enrollments_event_id ON enrollments(event_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);
CREATE INDEX idx_player_ratings_total_points ON player_ratings(total_points DESC);
CREATE INDEX idx_rating_entries_tournament_id ON rating_entries(tournament_id);

