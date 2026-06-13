--liquibase formatted sql

--changeset mik:V004_create_registrations
CREATE SEQUENCE registrations_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE registrations
(
    id         BIGINT PRIMARY KEY DEFAULT nextval('registrations_id_seq'),
    event_id   BIGINT                   NOT NULL REFERENCES events (id),
    user_id    BIGINT                   NOT NULL REFERENCES users (id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_registrations_event_user UNIQUE (event_id, user_id)
);

CREATE INDEX idx_registrations_event_id ON registrations (event_id);
CREATE INDEX idx_registrations_user_id ON registrations (user_id);

--rollback DROP TABLE registrations; DROP SEQUENCE registrations_id_seq;
