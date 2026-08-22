-- ============================================================
-- ORDER DATABASE SCHEMA
-- Service: order-service
-- Database: order_db
-- Host Port: 5439
-- Internal PostgreSQL Port: 5432
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. ORDERS TABLE
-- ============================================================
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(30) UNIQUE NOT NULL,
    user_id UUID NOT NULL,
    seller_id UUID,
    order_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    subtotal DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    shipping_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    coupon_id UUID,
    coupon_code VARCHAR(50),
    notes TEXT,
    cancellation_reason VARCHAR(500),
    return_reason VARCHAR(500),
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    returned_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_orders_status
        CHECK (order_status IN (
            'PENDING', 'CONFIRMED', 'PACKED', 'SHIPPED',
            'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED',
            'RETURN_REQUESTED', 'RETURNED', 'REFUNDED', 'FAILED'
        )),

    CONSTRAINT chk_orders_payment_status
        CHECK (payment_status IN (
            'PENDING', 'PAID', 'FAILED', 'REFUNDED',
            'PARTIALLY_REFUNDED', 'CANCELLED'
        )),

    CONSTRAINT chk_orders_amounts
        CHECK (
            subtotal >= 0 AND
            discount_amount >= 0 AND
            tax_amount >= 0 AND
            shipping_amount >= 0 AND
            total_amount >= 0
        )
);

-- ============================================================
-- 2. ORDER ITEMS TABLE
-- ============================================================
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    variant_id UUID,
    product_name VARCHAR(500) NOT NULL,
    product_image TEXT,
    product_sku VARCHAR(100),
    unit_price DECIMAL(19, 2) NOT NULL,
    selling_price DECIMAL(19, 2) NOT NULL,
    quantity INT NOT NULL,
    total_price DECIMAL(19, 2) NOT NULL,
    product_snapshot JSONB,
    item_status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    return_reason VARCHAR(500),
    returned_at TIMESTAMP,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_order_items_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_order_items_status
        CHECK (item_status IN ('ACTIVE', 'CANCELLED', 'RETURNED', 'REFUNDED')),

    CONSTRAINT chk_order_items_prices
        CHECK (unit_price >= 0 AND selling_price >= 0 AND total_price >= 0)
);

-- ============================================================
-- 3. ORDER STATUS HISTORY TABLE
-- ============================================================
CREATE TABLE order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,
    changed_by VARCHAR(200),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_status_history_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE
);

-- ============================================================
-- 4. ORDER ADDRESSES TABLE
-- ============================================================
CREATE TABLE order_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    address_type VARCHAR(20) NOT NULL DEFAULT 'SHIPPING',
    full_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address_line1 VARCHAR(300) NOT NULL,
    address_line2 VARCHAR(300),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    postal_code VARCHAR(20),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,

    CONSTRAINT fk_order_addresses_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_order_address_type
        CHECK (address_type IN ('SHIPPING', 'BILLING'))
);

-- ============================================================
-- 5. ORDER NOTES TABLE
-- ============================================================
CREATE TABLE order_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    added_by UUID,
    note_type VARCHAR(30) DEFAULT 'GENERAL',
    note TEXT NOT NULL,
    is_internal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_order_notes_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_order_note_type
        CHECK (note_type IN ('GENERAL', 'ADMIN', 'SELLER', 'DELIVERY'))
);

-- ============================================================
-- 6. OUTBOX EVENTS TABLE (Saga Pattern)
-- ============================================================
CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    error_message VARCHAR(500),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_outbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'SENT', 'FAILED'))
);

-- ============================================================
-- SEQUENCE & FUNCTION: Order Number Generator
-- ============================================================
CREATE SEQUENCE IF NOT EXISTS order_number_seq START WITH 1000 INCREMENT BY 1;

CREATE OR REPLACE FUNCTION generate_order_number()
RETURNS TEXT AS $$
DECLARE
    seq_val BIGINT;
    date_part TEXT;
BEGIN
    seq_val := NEXTVAL('order_number_seq');
    date_part := TO_CHAR(CURRENT_TIMESTAMP, 'YYYYMMDD');
    RETURN 'ORD-' || date_part || '-' || LPAD(seq_val::TEXT, 6, '0');
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_seller_id ON orders(seller_id);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_status ON orders(order_status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_orders_user_status ON orders(user_id, order_status);

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
CREATE INDEX idx_order_hist_order ON order_status_history(order_id);
CREATE INDEX idx_order_addr_order ON order_addresses(order_id);
CREATE INDEX idx_order_notes_order ON order_notes(order_id);

CREATE INDEX idx_outbox_status ON outbox_events(status);
CREATE INDEX idx_outbox_created ON outbox_events(created_at);
CREATE INDEX idx_outbox_pending ON outbox_events(status, created_at) WHERE status = 'PENDING';

-- ============================================================
-- TRIGGERS
-- ============================================================
CREATE OR REPLACE FUNCTION update_order_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_orders_updated_at
BEFORE UPDATE ON orders
FOR EACH ROW EXECUTE FUNCTION update_order_updated_at();

CREATE OR REPLACE FUNCTION calculate_order_item_total()
RETURNS TRIGGER AS $$
BEGIN
    NEW.total_price := NEW.selling_price * NEW.quantity;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_order_item_calculate_total
BEFORE INSERT OR UPDATE ON order_items
FOR EACH ROW EXECUTE FUNCTION calculate_order_item_total();

-- ============================================================
-- SEED DATA (Dynamic & Idempotent Pattern)
-- ============================================================
INSERT INTO orders (
    order_number, user_id, order_status, payment_status,
    subtotal, discount_amount, tax_amount, shipping_amount, total_amount
)
VALUES (
    'ORD-20260819-001001',
    '6b6d8b38-30c8-4b8f-bdce-e62386e8edf3',
    'CONFIRMED',
    'PAID',
    1099.00,
    0.00,
    54.95,
    5.00,
    1158.95
)
ON CONFLICT (order_number) DO NOTHING;

INSERT INTO order_items (
    order_id, product_id, product_name, product_sku, unit_price, selling_price, quantity, total_price
)
SELECT
    o.id,
    'e4b2d56a-1234-4567-890a-bcdef1234567',
    'Apple iPhone 15 Pro Max 256GB',
    'IPHONE-15-PM-256-TI',
    1199.00,
    1099.00,
    1,
    1099.00
FROM orders o
WHERE o.order_number = 'ORD-20260819-001001'
ON CONFLICT DO NOTHING;

INSERT INTO order_addresses (
    order_id, address_type, full_name, phone, address_line1, city, state, country, postal_code
)
SELECT
    o.id,
    'SHIPPING',
    'Apurba Customer',
    '+919876543210',
    '123 Tech Park Road',
    'Bengaluru',
    'Karnataka',
    'India',
    '560100'
FROM orders o
WHERE o.order_number = 'ORD-20260819-001001'
ON CONFLICT DO NOTHING;
