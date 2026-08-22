-- ============================================================
-- AUDIT DATABASE SCHEMA
-- Service: audit-service
-- Database: audit_db
-- Host Port: 5446
-- Internal PostgreSQL Port: 5432
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. AUDIT LOGS TABLE (Partitioned by Range on created_at)
-- ============================================================
CREATE TABLE audit_logs (
    id              UUID NOT NULL DEFAULT gen_random_uuid(),
    user_id         UUID,
    user_email      VARCHAR(200),
    user_role       VARCHAR(50),
    action          VARCHAR(100) NOT NULL,
    resource_type   VARCHAR(100),
    resource_id     VARCHAR(100),
    old_values      JSONB,
    new_values      JSONB,
    request_body    JSONB,
    http_method     VARCHAR(10),
    endpoint        VARCHAR(300),
    ip_address      VARCHAR(50),
    user_agent      VARCHAR(500),
    session_id      VARCHAR(200),
    correlation_id  VARCHAR(200),
    status          VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    error_message   VARCHAR(1000),
    duration_ms     INT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id, created_at),
    CONSTRAINT chk_audit_status CHECK (status IN ('SUCCESS', 'FAILURE'))
) PARTITION BY RANGE (created_at);

-- Declarative Partition Tables
CREATE TABLE audit_logs_2025_q1 PARTITION OF audit_logs FOR VALUES FROM ('2025-01-01') TO ('2025-04-01');
CREATE TABLE audit_logs_2025_q2 PARTITION OF audit_logs FOR VALUES FROM ('2025-04-01') TO ('2025-07-01');
CREATE TABLE audit_logs_2025_q3 PARTITION OF audit_logs FOR VALUES FROM ('2025-07-01') TO ('2025-10-01');
CREATE TABLE audit_logs_2025_q4 PARTITION OF audit_logs FOR VALUES FROM ('2025-10-01') TO ('2026-01-01');

CREATE TABLE audit_logs_2026_q1 PARTITION OF audit_logs FOR VALUES FROM ('2026-01-01') TO ('2026-04-01');
CREATE TABLE audit_logs_2026_q2 PARTITION OF audit_logs FOR VALUES FROM ('2026-04-01') TO ('2026-07-01');
CREATE TABLE audit_logs_2026_q3 PARTITION OF audit_logs FOR VALUES FROM ('2026-07-01') TO ('2026-10-01');
CREATE TABLE audit_logs_2026_q4 PARTITION OF audit_logs FOR VALUES FROM ('2026-10-01') TO ('2027-01-01');

CREATE TABLE audit_logs_default PARTITION OF audit_logs DEFAULT;

-- ============================================================
-- 2. SECURITY EVENTS TABLE
-- ============================================================
CREATE TABLE security_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID,
    event_type      VARCHAR(50) NOT NULL,
    ip_address      VARCHAR(50),
    device_info     VARCHAR(255),
    location        VARCHAR(200),
    success         BOOLEAN NOT NULL DEFAULT TRUE,
    failure_reason  VARCHAR(200),
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_security_event_type
        CHECK (event_type IN (
            'LOGIN_SUCCESS', 'LOGIN_FAILED', 'LOGOUT',
            'PASSWORD_CHANGE', 'PASSWORD_RESET', 'EMAIL_CHANGE',
            'ACCOUNT_LOCKED', 'ACCOUNT_UNLOCKED',
            'TWO_FA_ENABLED', 'TWO_FA_DISABLED', 'SUSPICIOUS_ACTIVITY'
        ))
);

-- ============================================================
-- 3. DATA ACCESS LOGS TABLE
-- ============================================================
CREATE TABLE data_access_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID,
    resource_type   VARCHAR(100) NOT NULL,
    resource_id     VARCHAR(100),
    access_type     VARCHAR(30) NOT NULL,
    ip_address      VARCHAR(50),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_data_access_type
        CHECK (access_type IN ('READ', 'WRITE', 'DELETE', 'EXPORT', 'BULK_READ'))
);

-- ============================================================
-- 4. SYSTEM EVENTS TABLE
-- ============================================================
CREATE TABLE system_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_name    VARCHAR(100) NOT NULL,
    event_type      VARCHAR(100) NOT NULL,
    severity        VARCHAR(20) NOT NULL DEFAULT 'INFO',
    message         TEXT NOT NULL,
    metadata        JSONB,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_system_severity
        CHECK (severity IN ('DEBUG', 'INFO', 'WARN', 'ERROR', 'CRITICAL'))
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_audit_user_id      ON audit_logs(user_id);
CREATE INDEX idx_audit_action       ON audit_logs(action);
CREATE INDEX idx_audit_resource     ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_created_at   ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_status       ON audit_logs(status);
CREATE INDEX idx_audit_correlation  ON audit_logs(correlation_id);

CREATE INDEX idx_security_user_id   ON security_events(user_id);
CREATE INDEX idx_security_type      ON security_events(event_type);
CREATE INDEX idx_security_date      ON security_events(created_at DESC);

CREATE INDEX idx_system_events_svc  ON system_events(service_name, severity);
CREATE INDEX idx_system_events_date ON system_events(created_at DESC);
