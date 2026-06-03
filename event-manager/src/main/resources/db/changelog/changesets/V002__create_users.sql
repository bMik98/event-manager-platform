--liquibase formatted sql

--changeset mik:V002_create_users
CREATE SEQUENCE users_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE users
(
    id            BIGINT PRIMARY KEY DEFAULT nextval('users_id_seq'),
    login         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    age           INTEGER      NOT NULL CHECK (age > 0)
);

--rollback DROP TABLE users; DROP SEQUENCE users_id_seq;
