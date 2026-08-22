-- ============================================================
-- INVENTORY DATABASE
-- Service: inventory-service
-- Database: inventory_db
-- Host Port: 5435
-- Internal PostgreSQL Port: 5432
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. WAREHOUSES
-- ============================================================
CREATE TABLE warehouses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    manager_name VARCHAR(150),
    manager_email VARCHAR(255),
    manager_phone VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_warehouses_code UNIQUE (code)
);

-- ============================================================
-- 2. INVENTORY
-- ============================================================
CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    variant_id UUID,
    warehouse_id UUID NOT NULL,
    seller_id UUID,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    sold_quantity INT NOT NULL DEFAULT 0,
    low_stock_threshold INT NOT NULL DEFAULT 0,
    track_inventory BOOLEAN NOT NULL DEFAULT TRUE,
    batch_number VARCHAR(100),
    expiry_date DATE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventory_warehouse
        FOREIGN KEY (warehouse_id)
        REFERENCES warehouses(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_inventory_quantity_non_negative
        CHECK (quantity >= 0),
    CONSTRAINT chk_inventory_reserved_non_negative
        CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_inventory_sold_non_negative
        CHECK (sold_quantity >= 0),
    CONSTRAINT chk_inventory_low_stock_threshold_non_negative
        CHECK (low_stock_threshold >= 0),
    CONSTRAINT chk_inventory_reserved_not_greater_than_quantity
        CHECK (reserved_quantity <= quantity)
);

-- ============================================================
-- 3. INVENTORY TRANSACTIONS
-- ============================================================
CREATE TABLE inventory_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_id UUID NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    quantity_before INT NOT NULL,
    quantity_after INT NOT NULL,
    reference_type VARCHAR(50),
    reference_id VARCHAR(255),
    note VARCHAR(500),
    performed_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_inventory_transactions_inventory
        FOREIGN KEY (inventory_id)
        REFERENCES inventory(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_inventory_transaction_quantity_non_negative
        CHECK (quantity >= 0),
    CONSTRAINT chk_inventory_transaction_before_non_negative
        CHECK (quantity_before >= 0),
    CONSTRAINT chk_inventory_transaction_after_non_negative
        CHECK (quantity_after >= 0)
);

-- ============================================================
-- 4. STOCK ALERTS
-- ============================================================
CREATE TABLE stock_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    inventory_id UUID NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    threshold_quantity INT NOT NULL,
    current_quantity INT NOT NULL,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_stock_alerts_inventory
        FOREIGN KEY (inventory_id)
        REFERENCES inventory(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_stock_alert_threshold_non_negative
        CHECK (threshold_quantity >= 0),
    CONSTRAINT chk_stock_alert_current_quantity_non_negative
        CHECK (current_quantity >= 0),
    CONSTRAINT chk_stock_alert_resolved_at
        CHECK (
            (is_resolved = FALSE AND resolved_at IS NULL)
            OR
            (is_resolved = TRUE AND resolved_at IS NOT NULL)
        )
);

-- ============================================================
-- 5. STOCK TRANSFERS
-- ============================================================
CREATE TABLE stock_transfers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    from_warehouse_id UUID NOT NULL,
    to_warehouse_id UUID NOT NULL,
    product_id UUID NOT NULL,
    variant_id UUID,
    quantity INT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    notes VARCHAR(500),
    initiated_by UUID,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_stock_transfers_from_warehouse
        FOREIGN KEY (from_warehouse_id)
        REFERENCES warehouses(id),
    CONSTRAINT fk_stock_transfers_to_warehouse
        FOREIGN KEY (to_warehouse_id)
        REFERENCES warehouses(id),
    CONSTRAINT chk_stock_transfer_quantity_positive
        CHECK (quantity > 0),
    CONSTRAINT chk_stock_transfer_different_warehouses
        CHECK (from_warehouse_id <> to_warehouse_id)
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_warehouses_city ON warehouses(city);
CREATE INDEX idx_warehouses_active ON warehouses(is_active);

CREATE INDEX idx_inventory_product_id ON inventory(product_id);
CREATE INDEX idx_inventory_variant_id ON inventory(variant_id);
CREATE INDEX idx_inventory_warehouse_id ON inventory(warehouse_id);
CREATE INDEX idx_inventory_seller_id ON inventory(seller_id);
CREATE INDEX idx_inventory_low_stock ON inventory(quantity, low_stock_threshold);

CREATE INDEX idx_inventory_transactions_inventory_id ON inventory_transactions(inventory_id);
CREATE INDEX idx_inventory_transactions_type ON inventory_transactions(transaction_type);
CREATE INDEX idx_inventory_transactions_reference ON inventory_transactions(reference_type, reference_id);
CREATE INDEX idx_inventory_transactions_created_at ON inventory_transactions(created_at);

CREATE INDEX idx_stock_alerts_inventory_id ON stock_alerts(inventory_id);
CREATE INDEX idx_stock_alerts_unresolved ON stock_alerts(is_resolved);
CREATE INDEX idx_stock_alerts_type ON stock_alerts(alert_type);

CREATE INDEX idx_stock_transfers_from_warehouse ON stock_transfers(from_warehouse_id);
CREATE INDEX idx_stock_transfers_to_warehouse ON stock_transfers(to_warehouse_id);
CREATE INDEX idx_stock_transfers_product_id ON stock_transfers(product_id);
CREATE INDEX idx_stock_transfers_status ON stock_transfers(status);
CREATE INDEX idx_stock_transfers_created_at ON stock_transfers(created_at);

CREATE UNIQUE INDEX uq_inventory_product_warehouse_no_variant
    ON inventory(product_id, warehouse_id)
    WHERE variant_id IS NULL;

CREATE UNIQUE INDEX uq_inventory_product_variant_warehouse
    ON inventory(product_id, variant_id, warehouse_id)
    WHERE variant_id IS NOT NULL;

CREATE UNIQUE INDEX uq_warehouses_one_default
    ON warehouses(is_default)
    WHERE is_default = TRUE;

-- ============================================================
-- TRIGGERS
-- ============================================================
CREATE OR REPLACE FUNCTION update_inventory_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_warehouses_updated_at
BEFORE UPDATE ON warehouses
FOR EACH ROW EXECUTE FUNCTION update_inventory_updated_at();

CREATE TRIGGER trg_inventory_updated_at
BEFORE UPDATE ON inventory
FOR EACH ROW EXECUTE FUNCTION update_inventory_updated_at();

CREATE OR REPLACE FUNCTION maintain_stock_alert_resolution()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_resolved = TRUE AND NEW.resolved_at IS NULL THEN
        NEW.resolved_at = CURRENT_TIMESTAMP;
    ELSIF NEW.is_resolved = FALSE THEN
        NEW.resolved_at = NULL;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_stock_alert_resolution
BEFORE INSERT OR UPDATE ON stock_alerts
FOR EACH ROW EXECUTE FUNCTION maintain_stock_alert_resolution();

-- ============================================================
-- SEED DATA (Phase-1 Dynamic & Idempotent Pattern)
-- ============================================================

INSERT INTO warehouses (name, code, address_line1, city, state, country, postal_code, latitude, longitude, manager_name, manager_email, manager_phone, is_active, is_default)
VALUES
    ('Central Logistics Hub', 'WH-CENTRAL-01', '1000 Distribution Way', 'Chicago', 'Illinois', 'USA', '60601', 41.8781, -87.6298, 'Marcus Vance', 'marcus.v@ecommerce.com', '+13125550100', TRUE, TRUE),
    ('West Coast Fulfillment Center', 'WH-WEST-02', '500 Pacific Coast Hwy', 'Los Angeles', 'California', 'USA', '90001', 34.0522, -118.2437, 'Elena Rostova', 'elena.r@ecommerce.com', '+12135550200', TRUE, FALSE),
    ('East Coast Distribution Center', 'WH-EAST-03', '750 Harbor Blvd', 'Newark', 'New Jersey', 'USA', '07101', 40.7357, -74.1724, 'David Chen', 'david.c@ecommerce.com', '+19735550300', TRUE, FALSE)
ON CONFLICT (code) DO NOTHING;

INSERT INTO inventory (product_id, variant_id, warehouse_id, quantity, reserved_quantity, sold_quantity, low_stock_threshold, track_inventory, batch_number)
SELECT
    'e4b2d56a-1234-4567-890a-bcdef1234567',
    NULL,
    w.id,
    150,
    5,
    25,
    20,
    TRUE,
    'BATCH-2026-Q1-001'
FROM warehouses w
WHERE w.code = 'WH-CENTRAL-01'
ON CONFLICT DO NOTHING;

INSERT INTO inventory (product_id, variant_id, warehouse_id, quantity, reserved_quantity, sold_quantity, low_stock_threshold, track_inventory, batch_number)
SELECT
    'f5c3e67b-2345-5678-901b-cdef01234568',
    NULL,
    w.id,
    12,
    0,
    88,
    15,
    TRUE,
    'BATCH-2026-Q1-003'
FROM warehouses w
WHERE w.code = 'WH-CENTRAL-01'
ON CONFLICT DO NOTHING;

INSERT INTO inventory_transactions (inventory_id, transaction_type, quantity, quantity_before, quantity_after, reference_type, reference_id, note)
SELECT
    i.id,
    'INITIAL_STOCK',
    150,
    0,
    150,
    'PURCHASE_ORDER',
    'PO-2026-0001',
    'Initial stock receipt from manufacturer'
FROM inventory i
JOIN warehouses w ON i.warehouse_id = w.id
WHERE w.code = 'WH-CENTRAL-01' AND i.product_id = 'e4b2d56a-1234-4567-890a-bcdef1234567'
ON CONFLICT DO NOTHING;

INSERT INTO stock_alerts (inventory_id, alert_type, threshold_quantity, current_quantity, is_resolved)
SELECT
    i.id,
    'LOW_STOCK',
    15,
    12,
    FALSE
FROM inventory i
JOIN warehouses w ON i.warehouse_id = w.id
WHERE w.code = 'WH-CENTRAL-01' AND i.product_id = 'f5c3e67b-2345-5678-901b-cdef01234568'
ON CONFLICT DO NOTHING;
