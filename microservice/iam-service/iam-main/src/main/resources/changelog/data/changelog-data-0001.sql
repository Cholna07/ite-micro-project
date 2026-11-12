--liquibase formatted sql

--changeset cholna:0001-create-default-user-role

INSERT INTO roles (
    uuid,
    name,
    description,
    is_system,
    is_active,
    is_deleted,
    created_at,
    created_by,
    version
)
VALUES (
           '550e8400-e29b-41d4-a716-446655440000',
           'USER',
           'Default role for regular application users',
           TRUE,
           TRUE,
           FALSE,
           CURRENT_TIMESTAMP,
           'SYSTEM',
           0
       );
