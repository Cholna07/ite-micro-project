ALTER TABLE iam_system_configuration
    ALTER COLUMN config_value TYPE TEXT;

INSERT INTO iam_system_configuration (config_key, config_value)
VALUES ('branding.logo-url', '')
ON CONFLICT (config_key) DO NOTHING;
