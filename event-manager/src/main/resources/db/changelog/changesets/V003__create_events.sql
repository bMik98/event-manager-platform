--liquibase formatted sql

--changeset mik:V003_create_events
CREATE SEQUENCE events_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE events
(
    id               BIGINT PRIMARY KEY DEFAULT nextval('events_id_seq'),
    name             VARCHAR(255)             NOT NULL,
    owner_id         BIGINT                   NOT NULL REFERENCES users (id),
    location_id      BIGINT                   NOT NULL REFERENCES locations (id),
    max_places       INTEGER                  NOT NULL CHECK (max_places > 0),
    cost             INTEGER                  NOT NULL CHECK (cost >= 0),
    duration_minutes INTEGER                  NOT NULL CHECK (duration_minutes > 0),
    start_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    status           VARCHAR(50)              NOT NULL,
    occupied_places  INTEGER                  NOT NULL DEFAULT 0
        CHECK (occupied_places BETWEEN 0 AND max_places)
);

CREATE INDEX idx_events_owner_id ON events (owner_id);
CREATE INDEX idx_events_location_id ON events (location_id);
CREATE INDEX idx_events_status ON events (status);

--rollback DROP TABLE events; DROP SEQUENCE events_id_seq;
