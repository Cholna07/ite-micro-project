CREATE TABLE account_activity_events (
    event_uuid UUID PRIMARY KEY,
    user_uuid UUID NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    description VARCHAR(500) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    CONSTRAINT fk_account_activity_events_user FOREIGN KEY (user_uuid)
        REFERENCES users(uuid) ON DELETE CASCADE,
    CONSTRAINT chk_account_activity_event_type CHECK (event_type IN (
        'PASSWORD_CHANGED',
        'PROFILE_UPDATED',
        'EMAIL_CHANGED',
        'SOCIAL_ACCOUNT_LINKED',
        'SOCIAL_ACCOUNT_UNLINKED',
        'SESSION_REVOKED',
        'CURRENT_SESSION_REVOKED',
        'OTHER_SESSIONS_REVOKED'
    ))
);

CREATE INDEX idx_account_activity_user_time
    ON account_activity_events (user_uuid, occurred_at DESC, event_uuid DESC);
