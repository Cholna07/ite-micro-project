-- Preserve expired assignment history while freeing the user/role pair for reassignment.
UPDATE user_roles
SET revoked_at = expires_at,
    reason = COALESCE(reason, 'EXPIRED')
WHERE revoked_at IS NULL
  AND expires_at IS NOT NULL
  AND expires_at <= CURRENT_TIMESTAMP;

-- Repair any pre-existing duplicate active rows deterministically before adding the guard.
WITH ranked_assignments AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY user_id, role_id
               ORDER BY assigned_at DESC, id DESC
           ) AS row_number
    FROM user_roles
    WHERE revoked_at IS NULL
)
UPDATE user_roles ur
SET revoked_at = CURRENT_TIMESTAMP,
    reason = COALESCE(ur.reason, 'DUPLICATE_ACTIVE_ASSIGNMENT')
FROM ranked_assignments ranked
WHERE ur.id = ranked.id
  AND ranked.row_number > 1;

-- Only one unrevoked assignment may exist per user/role. Revoked rows remain as history.
CREATE UNIQUE INDEX uq_user_roles_unrevoked_user_role
    ON user_roles (user_id, role_id)
    WHERE revoked_at IS NULL;
