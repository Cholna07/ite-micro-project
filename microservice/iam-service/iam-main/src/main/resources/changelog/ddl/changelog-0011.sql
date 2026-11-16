CREATE TABLE user_sessions (
    id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL,
    credential_type VARCHAR(20) NOT NULL,
    authentication_type VARCHAR(40) NOT NULL,
    provider VARCHAR(40),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_activity_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    revoked_at TIMESTAMP WITH TIME ZONE,
    revoked_by_user_id BIGINT,
    revocation_reason VARCHAR(40),
    CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_sessions_revoked_by FOREIGN KEY (revoked_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_user_sessions_credential_type CHECK (credential_type IN ('JWT', 'HTTP_SESSION')),
    CONSTRAINT chk_user_sessions_expiry CHECK (expires_at IS NULL OR expires_at > created_at)
);

CREATE INDEX idx_user_sessions_user_active ON user_sessions (user_id, revoked_at, expires_at);
CREATE INDEX idx_user_sessions_user_activity ON user_sessions (user_id, last_activity_at DESC);
