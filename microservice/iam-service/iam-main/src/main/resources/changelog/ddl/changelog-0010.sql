CREATE TABLE login_events (
    id UUID PRIMARY KEY,
    user_id BIGINT NULL,
    outcome VARCHAR(10) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    failure_category VARCHAR(50),
    CONSTRAINT fk_login_events_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT chk_login_events_outcome CHECK (outcome IN ('SUCCESS', 'FAILURE'))
);

CREATE INDEX idx_login_events_user_occurred_at ON login_events (user_id, occurred_at DESC);
CREATE INDEX idx_login_events_outcome_occurred_at ON login_events (outcome, occurred_at DESC);
