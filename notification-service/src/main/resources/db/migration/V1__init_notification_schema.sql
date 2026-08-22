-- ============================================================
-- NOTIFICATION DATABASE SCHEMA
-- Service: notification-service
-- Database: notification_db
-- Host Port: 5443
-- Internal PostgreSQL Port: 5432
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. NOTIFICATIONS TABLE
-- ============================================================
CREATE TABLE notifications (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    type                VARCHAR(50) NOT NULL,
    channel             VARCHAR(30) NOT NULL,
    title               VARCHAR(300) NOT NULL,
    message             TEXT NOT NULL,
    data                JSONB,
    is_read             BOOLEAN NOT NULL DEFAULT FALSE,
    read_at             TIMESTAMP,
    status              VARCHAR(30) NOT NULL DEFAULT 'SENT',
    error_message       TEXT,
    retry_count         INT NOT NULL DEFAULT 0,
    sent_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_notifications_channel
        CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP')),

    CONSTRAINT chk_notifications_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED', 'CANCELLED'))
);

-- ============================================================
-- 2. NOTIFICATION TEMPLATES TABLE
-- ============================================================
CREATE TABLE notification_templates (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_key        VARCHAR(100) UNIQUE NOT NULL,
    type                VARCHAR(50) NOT NULL,
    channel             VARCHAR(30) NOT NULL,
    subject             VARCHAR(300),
    body_html           TEXT,
    body_text           TEXT,
    variables           JSONB,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_templates_channel
        CHECK (channel IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP'))
);

-- ============================================================
-- 3. NOTIFICATION PREFERENCES TABLE
-- ============================================================
CREATE TABLE notification_preferences (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID UNIQUE NOT NULL,
    email_order_updates     BOOLEAN NOT NULL DEFAULT TRUE,
    email_promotions        BOOLEAN NOT NULL DEFAULT TRUE,
    email_review_updates    BOOLEAN NOT NULL DEFAULT TRUE,
    email_security          BOOLEAN NOT NULL DEFAULT TRUE,
    email_newsletter        BOOLEAN NOT NULL DEFAULT FALSE,
    sms_order_updates       BOOLEAN NOT NULL DEFAULT TRUE,
    sms_promotions          BOOLEAN NOT NULL DEFAULT FALSE,
    sms_otp                 BOOLEAN NOT NULL DEFAULT TRUE,
    push_order_updates      BOOLEAN NOT NULL DEFAULT TRUE,
    push_promotions         BOOLEAN NOT NULL DEFAULT TRUE,
    push_stock_alerts       BOOLEAN NOT NULL DEFAULT TRUE,
    push_price_drops        BOOLEAN NOT NULL DEFAULT TRUE,
    in_app_all              BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 4. PUSH TOKENS TABLE
-- ============================================================
CREATE TABLE push_tokens (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL,
    token               VARCHAR(500) NOT NULL,
    platform            VARCHAR(30) NOT NULL,
    device_id           VARCHAR(100),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    last_used_at        TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_push_platform
        CHECK (platform IN ('ANDROID', 'IOS', 'WEB'))
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_notifications_user_id      ON notifications(user_id);
CREATE INDEX idx_notifications_status       ON notifications(status);
CREATE INDEX idx_notifications_is_read      ON notifications(user_id, is_read);
CREATE INDEX idx_notifications_created_at   ON notifications(created_at DESC);
CREATE INDEX idx_templates_key             ON notification_templates(template_key);
CREATE INDEX idx_preferences_user_id       ON notification_preferences(user_id);

-- ============================================================
-- TRIGGER FUNCTION FOR UPDATED_AT
-- ============================================================
CREATE OR REPLACE FUNCTION update_notification_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_templates_updated_at
BEFORE UPDATE ON notification_templates
FOR EACH ROW EXECUTE FUNCTION update_notification_updated_at();

-- ============================================================
-- INITIAL SEED DATA (Idempotent Pattern)
-- ============================================================
INSERT INTO notification_templates (template_key, type, channel, subject, body_html, body_text)
VALUES
    ('ORDER_CONFIRMED', 'ORDER', 'EMAIL', 'Order Confirmation - #{orderNumber}', '<h1>Order Confirmed!</h1>', 'Thank you for your order.'),
    ('WELCOME_USER', 'WELCOME', 'EMAIL', 'Welcome to E-Commerce Platform!', '<h1>Welcome!</h1>', 'Welcome to our platform!')
ON CONFLICT (template_key) DO NOTHING;
