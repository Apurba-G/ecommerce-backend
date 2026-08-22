-- ============================================================
-- REPORT DATABASE SCHEMA
-- Service: report-service
-- Database: report_db
-- Host Port: 5444
-- Internal PostgreSQL Port: 5432
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. SALES REPORTS TABLE
-- ============================================================
CREATE TABLE sales_reports (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_type             VARCHAR(30) NOT NULL,
    report_date             DATE NOT NULL,
    start_date              DATE NOT NULL,
    end_date                DATE NOT NULL,
    total_orders            INT NOT NULL DEFAULT 0,
    total_revenue           DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    total_discounts         DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    total_tax               DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    net_sales               DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    average_order_value     DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    status                  VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_sales_report_type CHECK (report_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUAL')),
    CONSTRAINT uq_sales_report_date_type UNIQUE (report_date, report_type)
);

-- ============================================================
-- 2. PRODUCT SALES REPORTS TABLE
-- ============================================================
CREATE TABLE product_sales_reports (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id               UUID NOT NULL REFERENCES sales_reports(id) ON DELETE CASCADE,
    product_id              UUID NOT NULL,
    product_name            VARCHAR(300) NOT NULL,
    sku                     VARCHAR(100),
    units_sold              INT NOT NULL DEFAULT 0,
    gross_revenue           DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    discounts_applied       DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    net_revenue             DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 3. SELLER PAYOUT REPORTS TABLE
-- ============================================================
CREATE TABLE seller_payout_reports (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id               UUID NOT NULL REFERENCES sales_reports(id) ON DELETE CASCADE,
    seller_id               UUID NOT NULL,
    seller_name             VARCHAR(300) NOT NULL,
    total_sales             DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    platform_commission     DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    payout_amount           DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    status                  VARCHAR(30) NOT NULL DEFAULT 'SETTLED',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 4. CUSTOM REPORTS TABLE
-- ============================================================
CREATE TABLE custom_reports (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_name             VARCHAR(200) NOT NULL,
    user_id                 UUID NOT NULL,
    query_filters           JSONB,
    file_url                TEXT,
    status                  VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_custom_report_status CHECK (status IN ('PENDING', 'GENERATING', 'COMPLETED', 'FAILED'))
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_sales_reports_date ON sales_reports(report_date DESC);
CREATE INDEX idx_sales_reports_type ON sales_reports(report_type);
CREATE INDEX idx_prod_sales_report ON product_sales_reports(report_id, product_id);
CREATE INDEX idx_seller_payout_report ON seller_payout_reports(report_id, seller_id);
CREATE INDEX idx_custom_reports_user ON custom_reports(user_id);

-- ============================================================
-- INITIAL SEED DATA (Idempotent Pattern)
-- ============================================================
INSERT INTO sales_reports (
    report_type, report_date, start_date, end_date, total_orders, total_revenue, net_sales, average_order_value
) VALUES (
    'DAILY', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE, 150, 15000.00, 14200.00, 100.00
) ON CONFLICT (report_date, report_type) DO NOTHING;
