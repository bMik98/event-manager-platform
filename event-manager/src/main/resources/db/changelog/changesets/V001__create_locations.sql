--liquibase formatted sql

--changeset mik:V001_create_locations
CREATE TABLE locations
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    address     VARCHAR(500) NOT NULL,
    capacity    INTEGER      NOT NULL CHECK (capacity > 0),
    description TEXT
);

--rollback DROP TABLE locations;
