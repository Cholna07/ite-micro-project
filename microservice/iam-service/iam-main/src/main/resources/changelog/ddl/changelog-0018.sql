CREATE TABLE iam_system_configuration (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100)
);

INSERT INTO iam_system_configuration (config_key, config_value) VALUES
    ('registration.enabled', 'true'),
    ('password.minimum-length', '8'),
    ('authentication.max-failed-attempts', '5')
ON CONFLICT (config_key) DO NOTHING;

INSERT INTO permissions (
    uuid, name, resource, action, description, is_system, is_active, is_deleted,
    created_at, created_by, version
)
SELECT 'a18d17a0-0018-4000-8000-000000000001', 'system.config.read', 'system.config', 'read',
       'Read IAM system configuration', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP, 'SYSTEM', 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'system.config.read');

INSERT INTO permissions (
    uuid, name, resource, action, description, is_system, is_active, is_deleted,
    created_at, created_by, version
)
SELECT 'a18d17a0-0018-4000-8000-000000000002', 'system.config.manage', 'system.config', 'manage',
       'Manage IAM system configuration', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP, 'SYSTEM', 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'system.config.manage');

INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by, reason)
SELECT roles.id, permissions.id, CURRENT_TIMESTAMP, 'SYSTEM', 'Grant IAM system configuration access to administrators'
FROM roles CROSS JOIN permissions
WHERE roles.name = 'ADMIN'
  AND permissions.name IN ('system.config.read', 'system.config.manage')
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions existing
      WHERE existing.role_id = roles.id AND existing.permission_id = permissions.id
        AND existing.revoked_at IS NULL
  );
