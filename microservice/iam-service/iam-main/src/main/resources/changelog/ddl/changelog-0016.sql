-- Extend the activity constraint without rewriting or deleting historical activity rows.
ALTER TABLE account_activity_events
    DROP CONSTRAINT chk_account_activity_event_type;

ALTER TABLE account_activity_events
    ADD CONSTRAINT chk_account_activity_event_type CHECK (event_type IN (
        'PASSWORD_CHANGED',
        'PROFILE_UPDATED',
        'EMAIL_CHANGED',
        'SOCIAL_ACCOUNT_LINKED',
        'SOCIAL_ACCOUNT_UNLINKED',
        'SESSION_REVOKED',
        'CURRENT_SESSION_REVOKED',
        'OTHER_SESSIONS_REVOKED',
        'ACCOUNT_DEACTIVATED',
        'ACCOUNT_LOCKED',
        'ACCOUNT_UNLOCKED'
    ));
