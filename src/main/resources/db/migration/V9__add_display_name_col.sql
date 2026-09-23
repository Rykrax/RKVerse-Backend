ALTER TABLE users ADD COLUMN display_name VARCHAR(100) NULL AFTER email;

UPDATE users
SET display_name = username
WHERE display_name IS NULL