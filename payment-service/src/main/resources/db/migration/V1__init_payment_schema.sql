-- ============================================================
-- PAYMENT DATABASE SCHEMA
-- Service: payment-service
-- Database: payment_db
-- Host Port: 5440
-- Internal PostgreSQL Port: 5432
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. PAYMENTS TABLE
-- ============================================================
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    user_id UUID NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(5) NOT NULL DEFAULT 'INR',
    gateway VARCHAR(50),
    gateway_order_id VARCHAR(200),
    gateway_payment_id VARCHAR(200),
    gateway_signature VARCHAR(500),
    failure_reason VARCHAR(500),
    idempotency_key VARCHAR(200) UNIQUE,
    paid_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_payments_method
        CHECK (payment_method IN (
            'CREDIT_CARD', 'DEBIT_CARD', 'UPI',
            'NET_BANKING', 'CASH_ON_DELIVERY',
            'WALLET', 'GIFT_CARD', 'EMI'
        )),

    CONSTRAINT chk_payments_status
        CHECK (payment_status IN (
            'PENDING', 'PROCESSING', 'SUCCESS',
            'FAILED', 'REFUNDED',
            'PARTIALLY_REFUNDED', 'CANCELLED'
        )),

    CONSTRAINT chk_payments_amount
        CHECK (amount > 0)
);

-- ============================================================
-- 2. PAYMENT TRANSACTIONS TABLE
-- ============================================================
CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    gateway_reference VARCHAR(200),
    gateway_response JSONB,
    failure_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_tx_payment
        FOREIGN KEY (payment_id)
        REFERENCES payments(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_payment_tx_type
        CHECK (transaction_type IN ('CHARGE', 'REFUND', 'PARTIAL_REFUND', 'VOID', 'CAPTURE')),

    CONSTRAINT chk_payment_tx_status
        CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED'))
);

-- ============================================================
-- 3. REFUNDS TABLE
-- ============================================================
CREATE TABLE refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL,
    order_id UUID NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    gateway_refund_id VARCHAR(200),
    gateway_response JSONB,
    processed_by UUID,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_refunds_payment
        FOREIGN KEY (payment_id)
        REFERENCES payments(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_refunds_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),

    CONSTRAINT chk_refunds_amount
        CHECK (amount > 0)
);

-- ============================================================
-- 4. SAVED PAYMENT METHODS TABLE
-- ============================================================
CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    method_type VARCHAR(30) NOT NULL,
    card_last4 VARCHAR(4),
    card_brand VARCHAR(20),
    card_expiry VARCHAR(7),
    card_holder_name VARCHAR(200),
    upi_id VARCHAR(100),
    bank_name VARCHAR(100),
    gateway_token VARCHAR(500),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_payment_method_type
        CHECK (method_type IN ('CREDIT_CARD', 'DEBIT_CARD', 'UPI', 'NET_BANKING', 'WALLET'))
);

-- ============================================================
-- 5. PAYMENT DISPUTES TABLE
-- ============================================================
CREATE TABLE payment_disputes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID NOT NULL,
    dispute_id VARCHAR(200) UNIQUE,
    reason VARCHAR(200),
    status VARCHAR(30) DEFAULT 'OPEN',
    amount DECIMAL(19, 2),
    evidence JSONB,
    due_date TIMESTAMP,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_payment_disputes_payment
        FOREIGN KEY (payment_id)
        REFERENCES payments(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_dispute_status
        CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'WON', 'LOST', 'CLOSED'))
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_user_id ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(payment_status);
CREATE INDEX idx_payments_gateway_ord ON payments(gateway_order_id);
CREATE INDEX idx_payments_idempotency ON payments(idempotency_key);
CREATE INDEX idx_payments_created_at ON payments(created_at DESC);

CREATE INDEX idx_pay_tx_payment_id ON payment_transactions(payment_id);
CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_order_id ON refunds(order_id);
CREATE INDEX idx_refunds_status ON refunds(status);
CREATE INDEX idx_pay_methods_user ON payment_methods(user_id, is_active);
CREATE INDEX idx_disputes_payment ON payment_disputes(payment_id);

-- ============================================================
-- TRIGGERS
-- ============================================================
CREATE OR REPLACE FUNCTION update_payment_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_payments_updated_at
BEFORE UPDATE ON payments
FOR EACH ROW EXECUTE FUNCTION update_payment_updated_at();

CREATE TRIGGER trg_refunds_updated_at
BEFORE UPDATE ON refunds
FOR EACH ROW EXECUTE FUNCTION update_payment_updated_at();

-- ============================================================
-- SEED DATA (Idempotent Pattern)
-- ============================================================
INSERT INTO payments (
    order_id, user_id, payment_method, payment_status, amount, currency, gateway, idempotency_key, paid_at
) VALUES (
    'b1000000-0000-0000-0000-000000000001',
    '6b6d8b38-30c8-4b8f-bdce-e62386e8edf3',
    'CREDIT_CARD',
    'SUCCESS',
    1158.95,
    'USD',
    'MOCK_STRIPE',
    'IDEM-SEED-PAYMENT-001001',
    CURRENT_TIMESTAMP
) ON CONFLICT (idempotency_key) DO NOTHING;
