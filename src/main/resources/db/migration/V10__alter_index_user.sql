ALTER TABLE users DROP INDEX uq_users_username;

CREATE UNIQUE INDEX uq_users_username_active
    ON users (
              username,
        (IF(deleted_at IS NULL, 'ACTIVE', CAST(deleted_at AS CHAR)))
        );