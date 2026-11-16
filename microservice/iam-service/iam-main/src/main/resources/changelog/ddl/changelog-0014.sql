-- Keep expired assignments as history but release their user/permission pair.
UPDATE user_permissions
SET revoked_at = expires_at,
    reason = COALESCE(reason, 'EXPIRED')
WHERE revoked_at IS NULL
  AND expires_at IS NOT NULL
  AND expires_at <= CURRENT_TIMESTAMP;

-- Repair any duplicate unrevoked records before enforcing the invariant.
WITH ranked_assignments AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY user_id, permission_id
               ORDER BY assigned_at DESC, id DESC
           ) AS row_number
    FROM user_permissions
    WHERE revoked_at IS NULL
)
UPDATE user_permissions up
SET revoked_at = CURRENT_TIMESTAMP,
    reason = COALESCE(up.reason, 'DUPLICATE_ACTIVE_ASSIGNMENT')
FROM ranked_assignments ranked
WHERE up.id = ranked.id
  AND ranked.row_number > 1;

-- The assignment invariant is one unrevoked row per user/permission, regardless of effect.
CREATE UNIQUE INDEX uq_user_permissions_unrevoked_user_permission
    ON user_permissions (user_id, permission_id)
    WHERE revoked_at IS NULL;
