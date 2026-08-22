-- ============================================================
-- CART DATABASE
-- Service: cart-service
-- Database: cart_db
-- Host Port: 5436
-- Internal PostgreSQL Port: 5432
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. CARTS
-- ============================================================
CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    session_id VARCHAR(255),
    cart_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    coupon_id UUID,
    coupon_code VARCHAR(100),
    subtotal DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    shipping_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_cart_owner
        CHECK (user_id IS NOT NULL OR session_id IS NOT NULL),
    CONSTRAINT chk_carts_subtotal_non_negative
        CHECK (subtotal >= 0),
    CONSTRAINT chk_carts_discount_non_negative
        CHECK (discount_amount >= 0),
    CONSTRAINT chk_carts_tax_non_negative
        CHECK (tax_amount >= 0),
    CONSTRAINT chk_carts_shipping_non_negative
        CHECK (shipping_amount >= 0),
    CONSTRAINT chk_carts_total_non_negative
        CHECK (total_amount >= 0)
);

-- ============================================================
-- 2. CART ITEMS
-- ============================================================
CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    variant_id UUID,
    product_name VARCHAR(255) NOT NULL,
    product_image TEXT,
    unit_price DECIMAL(19, 2) NOT NULL,
    selling_price DECIMAL(19, 2) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(19, 2) NOT NULL,
    product_snapshot JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id)
        REFERENCES carts(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_cart_items_quantity_positive
        CHECK (quantity > 0),
    CONSTRAINT chk_cart_items_unit_price_non_negative
        CHECK (unit_price >= 0),
    CONSTRAINT chk_cart_items_selling_price_non_negative
        CHECK (selling_price >= 0),
    CONSTRAINT chk_cart_items_total_price_non_negative
        CHECK (total_price >= 0)
);

-- ============================================================
-- 3. SAVED CARTS
-- ============================================================
CREATE TABLE saved_carts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    cart_data JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_carts_user_id ON carts(user_id);
CREATE INDEX idx_carts_session_id ON carts(session_id);
CREATE INDEX idx_carts_status ON carts(cart_status);
CREATE INDEX idx_carts_expires_at ON carts(expires_at);

CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);
CREATE INDEX idx_cart_items_variant_id ON cart_items(variant_id);

CREATE INDEX idx_saved_carts_user_id ON saved_carts(user_id);

CREATE UNIQUE INDEX uq_one_active_cart_per_user
    ON carts(user_id)
    WHERE cart_status = 'ACTIVE' AND user_id IS NOT NULL;

CREATE UNIQUE INDEX uq_one_active_cart_per_session
    ON carts(session_id)
    WHERE cart_status = 'ACTIVE' AND session_id IS NOT NULL;

CREATE UNIQUE INDEX uq_cart_product_without_variant
    ON cart_items(cart_id, product_id)
    WHERE variant_id IS NULL;

CREATE UNIQUE INDEX uq_cart_product_with_variant
    ON cart_items(cart_id, product_id, variant_id)
    WHERE variant_id IS NOT NULL;

-- ============================================================
-- TRIGGERS & RECALCULATION
-- ============================================================
CREATE OR REPLACE FUNCTION update_cart_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_carts_updated_at
BEFORE UPDATE ON carts
FOR EACH ROW EXECUTE FUNCTION update_cart_updated_at();

CREATE TRIGGER trg_cart_items_updated_at
BEFORE UPDATE ON cart_items
FOR EACH ROW EXECUTE FUNCTION update_cart_updated_at();

CREATE OR REPLACE FUNCTION calculate_cart_item_total()
RETURNS TRIGGER AS $$
BEGIN
    NEW.total_price := NEW.selling_price * NEW.quantity;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_cart_item_calculate_total
BEFORE INSERT OR UPDATE ON cart_items
FOR EACH ROW EXECUTE FUNCTION calculate_cart_item_total();

CREATE OR REPLACE FUNCTION recalculate_cart_totals(p_cart_id UUID)
RETURNS VOID AS $$
DECLARE
    calculated_subtotal DECIMAL(19, 2);
BEGIN
    SELECT COALESCE(SUM(total_price), 0.00)
    INTO calculated_subtotal
    FROM cart_items
    WHERE cart_id = p_cart_id;

    UPDATE carts
    SET
        subtotal = calculated_subtotal,
        total_amount = calculated_subtotal - discount_amount + tax_amount + shipping_amount,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = p_cart_id;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION trigger_recalculate_cart_totals()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        PERFORM recalculate_cart_totals(NEW.cart_id);
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        PERFORM recalculate_cart_totals(OLD.cart_id);
        RETURN OLD;
    ELSIF TG_OP = 'UPDATE' THEN
        PERFORM recalculate_cart_totals(OLD.cart_id);
        IF NEW.cart_id <> OLD.cart_id THEN
            PERFORM recalculate_cart_totals(NEW.cart_id);
        END IF;
        RETURN NEW;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_recalculate_cart_totals
AFTER INSERT OR UPDATE OR DELETE ON cart_items
FOR EACH ROW EXECUTE FUNCTION trigger_recalculate_cart_totals();

-- ============================================================
-- SEED DATA (Phase-1 Dynamic & Idempotent Pattern)
-- ============================================================

INSERT INTO carts (user_id, session_id, cart_status, shipping_amount, expires_at)
VALUES
    ('6b6d8b38-30c8-4b8f-bdce-e62386e8edf3', NULL, 'ACTIVE', 5.00, CURRENT_TIMESTAMP + INTERVAL '7 days')
ON CONFLICT DO NOTHING;

INSERT INTO carts (user_id, session_id, cart_status, shipping_amount, expires_at)
VALUES
    (NULL, 'GUEST-SESSION-DEMO-001', 'ACTIVE', 0.00, CURRENT_TIMESTAMP + INTERVAL '2 days')
ON CONFLICT DO NOTHING;

INSERT INTO cart_items (cart_id, product_id, variant_id, product_name, product_image, unit_price, selling_price, quantity, product_snapshot)
SELECT
    c.id,
    'e4b2d56a-1234-4567-890a-bcdef1234567',
    NULL,
    'Apple iPhone 15 Pro Max 256GB',
    'https://images.unsplash.com/photo-1695048133142-1a20484d2569',
    1199.00,
    1099.00,
    1,
    '{"name": "Apple iPhone 15 Pro Max", "brand": "Apple", "color": "Natural Titanium"}'::jsonb
FROM carts c
WHERE c.user_id = '6b6d8b38-30c8-4b8f-bdce-e62386e8edf3' AND c.cart_status = 'ACTIVE'
ON CONFLICT DO NOTHING;

INSERT INTO saved_carts (user_id, name, cart_data)
VALUES
    (
        '6b6d8b38-30c8-4b8f-bdce-e62386e8edf3',
        'Holiday Shopping List',
        '{"items": [{"productId": "e4b2d56a-1234-4567-890a-bcdef1234567", "quantity": 1}]}'::jsonb
    )
ON CONFLICT DO NOTHING;
