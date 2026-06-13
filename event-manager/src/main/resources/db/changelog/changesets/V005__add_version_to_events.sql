--liquibase formatted sql

--changeset mik:V005_add_version_to_events
ALTER TABLE events
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

--rollback ALTER TABLE events DROP COLUMN version;
