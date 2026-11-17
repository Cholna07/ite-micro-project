CREATE TABLE admin_audit_events (
    event_uuid UUID PRIMARY KEY,
    actor_uuid UUID,
    action_type VARCHAR(80) NOT NULL,
    target_type VARCHAR(40) NOT NULL,
    target_id VARCHAR(200) NOT NULL,
    outcome VARCHAR(10) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    details TEXT,
    CONSTRAINT chk_admin_audit_events_outcome CHECK (outcome IN ('SUCCESS', 'FAILURE'))
);

CREATE INDEX idx_admin_audit_events_occurred_at
    ON admin_audit_events (occurred_at DESC, event_uuid DESC);
CREATE INDEX idx_admin_audit_events_actor_time
    ON admin_audit_events (actor_uuid, occurred_at DESC, event_uuid DESC);
CREATE INDEX idx_admin_audit_events_action_time
    ON admin_audit_events (action_type, occurred_at DESC, event_uuid DESC);
CREATE INDEX idx_admin_audit_events_target_time
    ON admin_audit_events (target_type, target_id, occurred_at DESC, event_uuid DESC);
CREATE INDEX idx_admin_audit_events_outcome_time
    ON admin_audit_events (outcome, occurred_at DESC, event_uuid DESC);

-- Provision audit.read on existing installations and grant it to the existing ADMIN role.
INSERT INTO permissions (
    uuid, name, resource, action, description, is_system, is_active, is_deleted,
    created_at, created_by, version
)
SELECT 'a17d17a0-0017-4000-8000-000000000001', 'audit.read', 'audit', 'read',
       'Read administrative audit history', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP, 'SYSTEM', 0
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'audit.read');

INSERT INTO role_permissions (role_id, permission_id, assigned_at, assigned_by, reason)
SELECT roles.id, permissions.id, CURRENT_TIMESTAMP, 'SYSTEM', 'Grant audit log access to administrators'
FROM roles
CROSS JOIN permissions
WHERE roles.name = 'ADMIN'
  AND permissions.name = 'audit.read'
  AND NOT EXISTS (
      SELECT 1 FROM role_permissions existing
      WHERE existing.role_id = roles.id
        AND existing.permission_id = permissions.id
        AND existing.revoked_at IS NULL
  );
