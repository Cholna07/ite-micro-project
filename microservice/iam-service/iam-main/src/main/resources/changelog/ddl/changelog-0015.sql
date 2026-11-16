-- Expired assignments retain their rows but no longer occupy the active assignment key.
UPDATE role_permissions
SET revoked_at = expires_at,
    reason = COALESCE(reason, 'EXPIRED')
WHERE revoked_at IS NULL
  AND expires_at IS NOT NULL
  AND expires_at <= CURRENT_TIMESTAMP;

-- Keep the newest historical assignment active if legacy/concurrent duplicates exist.
WITH ranked_assignments AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY role_id, permission_id
               ORDER BY assigned_at DESC, id DESC
           ) AS row_number
    FROM role_permissions
    WHERE revoked_at IS NULL
)
UPDATE role_permissions rp
SET revoked_at = CURRENT_TIMESTAMP,
    reason = COALESCE(rp.reason, 'DUPLICATE_ACTIVE_ASSIGNMENT')
FROM ranked_assignments ranked
WHERE rp.id = ranked.id
  AND ranked.row_number > 1;

CREATE UNIQUE INDEX uq_role_permissions_unrevoked_role_permission
    ON role_permissions (role_id, permission_id)
    WHERE revoked_at IS NULL;
