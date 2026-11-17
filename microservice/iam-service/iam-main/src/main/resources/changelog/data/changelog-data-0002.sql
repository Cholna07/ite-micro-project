--liquibase formatted sql

--changeset cholna:0002-assign-default-user-role-to-roleless-accounts
INSERT INTO user_roles (user_id, role_id, assigned_at, assigned_by, reason)
SELECT u.id, r.id, CURRENT_TIMESTAMP, 'SYSTEM', 'Default USER role assigned to existing account'
FROM users u
CROSS JOIN roles r
WHERE u.is_deleted = FALSE
  AND r.name = 'USER'
  AND r.is_active = TRUE
  AND r.is_deleted = FALSE
  AND NOT EXISTS (
      SELECT 1
      FROM user_roles existing_assignment
      JOIN roles existing_role ON existing_role.id = existing_assignment.role_id
      WHERE existing_assignment.user_id = u.id
        AND existing_assignment.revoked_at IS NULL
        AND (existing_assignment.expires_at IS NULL OR existing_assignment.expires_at > CURRENT_TIMESTAMP)
        AND existing_role.is_active = TRUE
        AND existing_role.is_deleted = FALSE
  );
