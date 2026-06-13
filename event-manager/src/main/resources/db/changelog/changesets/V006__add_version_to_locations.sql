--liquibase formatted sql

--changeset mik:V006_add_version_to_locations
ALTER TABLE locations
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

--rollback ALTER TABLE locations DROP COLUMN version;
