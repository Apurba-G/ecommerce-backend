-- ============================================================
-- COUPON DATABASE SCHEMA
-- Service: coupon-service
-- Database: coupon_db
-- Host Port: 5441
-- Internal PostgreSQL Port: 5432
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. COUPONS TABLE
-- ============================================================
CREATE TABLE coupons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    discount_type VARCHAR(30) NOT NULL,
    discount_value DECIMAL(19, 2) NOT NULL,
    max_discount_amount DECIMAL(19, 2),
    minimum_order_value DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    usage_limit INT,
    usage_limit_per_user INT NOT NULL DEFAULT 1,
    used_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    seller_id UUID,
    category_id UUID,
    product_id UUID,
    applicable_for VARCHAR(30) DEFAULT 'ALL',
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_coupons_discount_type
        CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT', 'FREE_SHIPPING', 'BUY_ONE_GET_ONE')),

    CONSTRAINT chk_coupons_applicable_for
        CHECK (applicable_for IN ('ALL', 'CATEGORY', 'PRODUCT', 'SELLER', 'FIRST_ORDER')),

    CONSTRAINT chk_coupons_values_non_negative
        CHECK (discount_value >= 0 AND (max_discount_amount IS NULL OR max_discount_amount >= 0) AND minimum_order_value >= 0),

    CONSTRAINT chk_coupons_percentage_range
        CHECK (
            (discount_type = 'PERCENTAGE' AND discount_value BETWEEN 0 AND 100)
            OR discount_type != 'PERCENTAGE'
        )
);

-- ============================================================
-- 2. COUPON USAGE TABLE
-- ============================================================
CREATE TABLE coupon_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    coupon_id UUID NOT NULL,
    user_id UUID NOT NULL,
    order_id UUID UNIQUE,
    discount_applied DECIMAL(19, 2),
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_coupon_usage_coupon
        FOREIGN KEY (coupon_id)
        REFERENCES coupons(id)
        ON DELETE CASCADE,

    CONSTRAINT uq_coupon_user_order UNIQUE (coupon_id, order_id)
);

-- ============================================================
-- 3. COUPON RESTRICTIONS TABLE
-- ============================================================
CREATE TABLE coupon_restrictions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    coupon_id UUID NOT NULL,
    restriction_type VARCHAR(50) NOT NULL,
    restriction_value VARCHAR(500),

    CONSTRAINT fk_coupon_restrictions_coupon
        FOREIGN KEY (coupon_id)
        REFERENCES coupons(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_coupon_restriction_type
        CHECK (restriction_type IN (
            'MIN_PURCHASE', 'MAX_USES_PER_DAY',
            'USER_SEGMENT', 'PAYMENT_METHOD',
            'FIRST_ORDER_ONLY', 'NEW_USER_ONLY'
        ))
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_coupons_code ON coupons(code);
CREATE INDEX idx_coupons_active ON coupons(is_active, valid_until);
CREATE INDEX idx_coupons_public ON coupons(is_public, is_active);
CREATE INDEX idx_coupons_seller ON coupons(seller_id);
CREATE INDEX idx_coupons_category ON coupons(category_id);
CREATE INDEX idx_coupons_product ON coupons(product_id);

CREATE INDEX idx_coupon_usage_user ON coupon_usage(user_id);
CREATE INDEX idx_coupon_usage_coupon ON coupon_usage(coupon_id);
CREATE INDEX idx_coupon_usage_order ON coupon_usage(order_id);

CREATE INDEX idx_coupon_restrictions_coupon ON coupon_restrictions(coupon_id);

-- ============================================================
-- TRIGGERS
-- ============================================================
CREATE OR REPLACE FUNCTION update_coupons_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_coupons_updated_at
BEFORE UPDATE ON coupons
FOR EACH ROW EXECUTE FUNCTION update_coupons_updated_at();

-- ============================================================
-- FUNCTION: Validate & Calculate Coupon Discount
-- ============================================================
CREATE OR REPLACE FUNCTION validate_coupon(
    p_code TEXT,
    p_user_id UUID,
    p_order_amount DECIMAL(19, 2),
    p_order_id UUID DEFAULT NULL
)
RETURNS TABLE (
    is_valid BOOLEAN,
    discount_amount DECIMAL(19, 2),
    message TEXT
) AS $$
DECLARE
    v_coupon RECORD;
    v_usage_count BIGINT;
    v_discount DECIMAL(19, 2) := 0.00;
BEGIN
    SELECT * INTO v_coupon
    FROM coupons
    WHERE code = UPPER(TRIM(p_code)) AND is_active = TRUE;

    IF NOT FOUND THEN
        RETURN QUERY SELECT FALSE, 0.00::DECIMAL(19, 2), 'Invalid coupon code';
        RETURN;
    END IF;

    IF v_coupon.valid_from IS NOT NULL AND CURRENT_TIMESTAMP < v_coupon.valid_from THEN
        RETURN QUERY SELECT FALSE, 0.00::DECIMAL(19, 2), 'Coupon not yet active';
        RETURN;
    END IF;

    IF v_coupon.valid_until IS NOT NULL AND CURRENT_TIMESTAMP > v_coupon.valid_until THEN
        RETURN QUERY SELECT FALSE, 0.00::DECIMAL(19, 2), 'Coupon has expired';
        RETURN;
    END IF;

    IF v_coupon.usage_limit IS NOT NULL AND v_coupon.used_count >= v_coupon.usage_limit THEN
        RETURN QUERY SELECT FALSE, 0.00::DECIMAL(19, 2), 'Coupon usage limit reached';
        RETURN;
    END IF;

    IF p_order_amount < v_coupon.minimum_order_value THEN
        RETURN QUERY SELECT
            FALSE,
            0.00::DECIMAL(19, 2),
            FORMAT('Minimum order value %s required for this coupon', v_coupon.minimum_order_value::TEXT);
        RETURN;
    END IF;

    SELECT COUNT(*) INTO v_usage_count
    FROM coupon_usage
    WHERE coupon_id = v_coupon.id AND user_id = p_user_id;

    IF v_usage_count >= v_coupon.usage_limit_per_user THEN
        RETURN QUERY SELECT FALSE, 0.00::DECIMAL(19, 2), 'You have reached maximum usage limit for this coupon';
        RETURN;
    END IF;

    v_discount := CASE v_coupon.discount_type
        WHEN 'PERCENTAGE' THEN
            LEAST(
                p_order_amount * v_coupon.discount_value / 100.00,
                COALESCE(v_coupon.max_discount_amount, p_order_amount)
            )
        WHEN 'FIXED_AMOUNT' THEN
            LEAST(v_coupon.discount_value, p_order_amount)
        WHEN 'FREE_SHIPPING' THEN 0.00
        ELSE 0.00
    END;

    RETURN QUERY SELECT TRUE, v_discount, 'Coupon applied successfully';
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- SEED DATA (Idempotent Pattern)
-- ============================================================
INSERT INTO coupons (
    code, name, description, discount_type, discount_value,
    minimum_order_value, usage_limit, is_active, is_public, valid_until
) VALUES
    ('WELCOME10', 'Welcome Discount', '10% off on your first order', 'PERCENTAGE', 10.00, 200.00, 10000, TRUE, TRUE, CURRENT_TIMESTAMP + INTERVAL '1 year'),
    ('FLAT50', 'Flat $50 Off', 'Get flat $50 discount on orders above $300', 'FIXED_AMOUNT', 50.00, 300.00, 5000, TRUE, TRUE, CURRENT_TIMESTAMP + INTERVAL '6 months'),
    ('FREESHIP', 'Free Shipping Voucher', 'Free standard shipping on orders over $100', 'FREE_SHIPPING', 0.00, 100.00, NULL, TRUE, TRUE, CURRENT_TIMESTAMP + INTERVAL '3 months'),
    ('SAVE20', 'Save 20%', '20% discount up to $100 off', 'PERCENTAGE', 20.00, 500.00, 2000, TRUE, TRUE, CURRENT_TIMESTAMP + INTERVAL '2 months'),
    ('FESTIVE15', 'Festive Sale 15% Off', '15% off site-wide store discount', 'PERCENTAGE', 15.00, 0.00, 50000, TRUE, TRUE, CURRENT_TIMESTAMP + INTERVAL '30 days')
ON CONFLICT (code) DO NOTHING;
