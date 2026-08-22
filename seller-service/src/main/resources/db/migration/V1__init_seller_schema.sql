-- ============================================================
-- SELLER DATABASE SCHEMA
-- Service: seller-service
-- Database: seller_db
-- Host Port: 5445
-- Internal PostgreSQL Port: 5432
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. SELLER PROFILES TABLE
-- ============================================================
CREATE TABLE seller_profiles (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id                 UUID UNIQUE NOT NULL,
    business_name           VARCHAR(300) UNIQUE NOT NULL,
    business_type           VARCHAR(100),
    gstin                   VARCHAR(20),
    pan_number              VARCHAR(10),
    business_address        VARCHAR(500),
    business_city           VARCHAR(100),
    business_state          VARCHAR(100),
    business_country        VARCHAR(100) DEFAULT 'India',
    business_postal_code    VARCHAR(20),
    bank_account_number     VARCHAR(20),
    bank_ifsc               VARCHAR(11),
    bank_name               VARCHAR(200),
    bank_account_holder     VARCHAR(200),
    seller_status           VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rejection_reason        VARCHAR(500),
    commission_rate         DECIMAL(5, 2) NOT NULL DEFAULT 10.00,
    total_revenue           DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_payouts           DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    pending_payout          DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    total_orders            INT NOT NULL DEFAULT 0,
    total_products          INT NOT NULL DEFAULT 0,
    rating                  DECIMAL(3, 2) NOT NULL DEFAULT 0.00,
    review_count            INT NOT NULL DEFAULT 0,
    return_rate             DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    cancellation_rate       DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    is_verified             BOOLEAN NOT NULL DEFAULT FALSE,
    is_featured             BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at             TIMESTAMP,
    suspended_at            TIMESTAMP,
    suspension_reason       VARCHAR(500),
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_seller_business_type
        CHECK (business_type IN (
            'INDIVIDUAL', 'SOLE_PROPRIETORSHIP',
            'PARTNERSHIP', 'PVT_LTD', 'LTD', 'LLP'
        )),

    CONSTRAINT chk_seller_status
        CHECK (seller_status IN (
            'PENDING', 'ACTIVE', 'SUSPENDED', 'REJECTED', 'BANNED'
        )),

    CONSTRAINT chk_seller_commission_rate
        CHECK (commission_rate BETWEEN 0.00 AND 100.00),

    CONSTRAINT chk_seller_rating
        CHECK (rating BETWEEN 0.00 AND 5.00)
);

-- ============================================================
-- 2. SELLER DOCUMENTS TABLE
-- ============================================================
CREATE TABLE seller_documents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id           UUID NOT NULL REFERENCES seller_profiles(id) ON DELETE CASCADE,
    document_type       VARCHAR(50) NOT NULL,
    document_url        TEXT NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    rejection_reason    VARCHAR(500),
    verified_at         TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_seller_doc_type
        CHECK (document_type IN (
            'PAN_CARD', 'GSTIN', 'BUSINESS_REGISTRATION',
            'BANK_STATEMENT', 'CANCELLED_CHEQUE',
            'ADDRESS_PROOF', 'ID_PROOF'
        )),

    CONSTRAINT chk_seller_doc_status
        CHECK (status IN ('PENDING', 'VERIFIED', 'REJECTED'))
);

-- ============================================================
-- 3. SELLER PAYOUTS TABLE
-- ============================================================
CREATE TABLE seller_payouts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id           UUID NOT NULL REFERENCES seller_profiles(id) ON DELETE CASCADE,
    amount              DECIMAL(10, 2) NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_method      VARCHAR(50),
    transaction_id      VARCHAR(200),
    bank_account        VARCHAR(20),
    bank_ifsc           VARCHAR(11),
    notes               TEXT,
    processed_at        TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_seller_payout_amount CHECK (amount > 0),
    CONSTRAINT chk_seller_payout_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

-- ============================================================
-- 4. SELLER COMMISSIONS TABLE
-- ============================================================
CREATE TABLE seller_commissions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id           UUID NOT NULL REFERENCES seller_profiles(id) ON DELETE CASCADE,
    order_id            UUID NOT NULL,
    order_item_id       UUID NOT NULL,
    sale_amount         DECIMAL(10, 2) NOT NULL,
    commission_rate     DECIMAL(5, 2) NOT NULL,
    commission_amount   DECIMAL(10, 2) NOT NULL,
    platform_fee        DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    gst_on_commission   DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    seller_earning      DECIMAL(10, 2) NOT NULL,
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payout_id           UUID REFERENCES seller_payouts(id) ON DELETE SET NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_seller_comm_status CHECK (status IN ('PENDING', 'SETTLED', 'REVERSED'))
);

-- ============================================================
-- 5. SELLER ANALYTICS TABLE
-- ============================================================
CREATE TABLE seller_analytics (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id           UUID NOT NULL REFERENCES seller_profiles(id) ON DELETE CASCADE,
    report_date         DATE NOT NULL,
    total_orders        INT NOT NULL DEFAULT 0,
    delivered_orders    INT NOT NULL DEFAULT 0,
    cancelled_orders    INT NOT NULL DEFAULT 0,
    returned_orders     INT NOT NULL DEFAULT 0,
    gross_revenue       DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    net_revenue         DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    commission_paid     DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    refunds_issued      DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    new_products        INT NOT NULL DEFAULT 0,
    page_views          INT NOT NULL DEFAULT 0,
    average_rating      DECIMAL(3, 2) NOT NULL DEFAULT 0.00,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_seller_report_date UNIQUE (seller_id, report_date)
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_seller_user_id      ON seller_profiles(user_id);
CREATE INDEX idx_seller_status       ON seller_profiles(seller_status);
CREATE INDEX idx_seller_verified     ON seller_profiles(is_verified);
CREATE INDEX idx_seller_featured     ON seller_profiles(is_featured);
CREATE INDEX idx_seller_docs_seller  ON seller_documents(seller_id);
CREATE INDEX idx_seller_docs_status  ON seller_documents(status);
CREATE INDEX idx_seller_payouts_sel  ON seller_payouts(seller_id);
CREATE INDEX idx_seller_payouts_sts  ON seller_payouts(status);
CREATE INDEX idx_seller_comm_seller  ON seller_commissions(seller_id);
CREATE INDEX idx_seller_comm_order   ON seller_commissions(order_id);
CREATE INDEX idx_seller_comm_status  ON seller_commissions(status);
CREATE INDEX idx_seller_analytics    ON seller_analytics(seller_id, report_date DESC);

-- ============================================================
-- TRIGGER FUNCTION FOR UPDATED_AT
-- ============================================================
CREATE OR REPLACE FUNCTION update_seller_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_seller_profiles_updated_at
BEFORE UPDATE ON seller_profiles
FOR EACH ROW EXECUTE FUNCTION update_seller_updated_at();

-- ============================================================
-- INITIAL SEED DATA (Idempotent Pattern)
-- ============================================================
INSERT INTO seller_profiles (
    user_id, business_name, business_type, gstin, pan_number,
    business_address, business_city, business_state, bank_account_number,
    bank_ifsc, bank_name, bank_account_holder, seller_status, is_verified, is_featured
) VALUES (
    'b2000000-0000-0000-0000-000000000001',
    'TechGadgets Official Store',
    'PVT_LTD',
    '29ABCDE1234F1Z5',
    'ABCDE1234F',
    '100 Tech Park, Electronic City',
    'Bengaluru',
    'Karnataka',
    '987654321098',
    'HDFC0001234',
    'HDFC Bank',
    'TechGadgets Pvt Ltd',
    'ACTIVE',
    TRUE,
    TRUE
) ON CONFLICT (business_name) DO NOTHING;
