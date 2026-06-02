--liquibase formatted sql

--changeset mik:V001_create_locations
CREATE SEQUENCE locations_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE locations
(
    id          BIGINT       PRIMARY KEY DEFAULT nextval('locations_id_seq'),
    name        VARCHAR(255) NOT NULL,
    address     VARCHAR(500) NOT NULL,
    capacity    INTEGER      NOT NULL CHECK (capacity > 0),
    description TEXT
);

--rollback DROP TABLE locations; DROP SEQUENCE locations_id_seq;
