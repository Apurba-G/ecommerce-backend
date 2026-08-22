-- ============================================================
-- SHIPPING DATABASE SCHEMA
-- Service: shipping-service
-- Database: shipping_db
-- Host Port: 5442
-- Internal PostgreSQL Port: 5432
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. SHIPMENTS TABLE
-- ============================================================
CREATE TABLE shipments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    user_id UUID NOT NULL,
    tracking_number VARCHAR(100) NOT NULL UNIQUE,
    carrier VARCHAR(100) NOT NULL,
    carrier_tracking_url TEXT,
    shipment_status VARCHAR(40) NOT NULL DEFAULT 'PENDING',
    shipping_method VARCHAR(50) NOT NULL,
    shipping_cost DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    estimated_weight DECIMAL(10, 3) NOT NULL DEFAULT 0.000,
    recipient_name VARCHAR(200) NOT NULL,
    recipient_phone VARCHAR(30) NOT NULL,
    street_address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    pin_code VARCHAR(20),
    shipped_at TIMESTAMP,
    estimated_delivery TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_shipment_shipping_cost
        CHECK (shipping_cost >= 0),

    CONSTRAINT chk_shipment_weight
        CHECK (estimated_weight >= 0),

    CONSTRAINT chk_shipment_delivery_dates
        CHECK (
            delivered_at IS NULL
            OR shipped_at IS NULL
            OR delivered_at >= shipped_at
        )
);

-- ============================================================
-- 2. SHIPMENT TRACKING TABLE
-- ============================================================
CREATE TABLE shipment_tracking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    location VARCHAR(255),
    description TEXT,
    activity_code VARCHAR(100),
    event_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_shipment_tracking_shipment
        FOREIGN KEY (shipment_id)
        REFERENCES shipments(id)
        ON DELETE CASCADE
);

-- ============================================================
-- 3. SHIPPING ZONES TABLE
-- ============================================================
CREATE TABLE shipping_zones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    zone_name VARCHAR(150) NOT NULL,
    country VARCHAR(100) NOT NULL,
    states JSONB,
    postal_codes JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 4. SHIPPING RATES TABLE
-- ============================================================
CREATE TABLE shipping_rates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    zone_id UUID NOT NULL,
    method_name VARCHAR(100) NOT NULL,
    carrier VARCHAR(100) NOT NULL,
    base_rate DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    per_kg_rate DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    min_weight DECIMAL(10, 3) NOT NULL DEFAULT 0.000,
    max_weight DECIMAL(10, 3),
    estimated_days_min INT NOT NULL DEFAULT 1,
    estimated_days_max INT NOT NULL DEFAULT 1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_shipping_rates_zone
        FOREIGN KEY (zone_id)
        REFERENCES shipping_zones(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_shipping_rate_base_rate
        CHECK (base_rate >= 0),

    CONSTRAINT chk_shipping_rate_per_kg
        CHECK (per_kg_rate >= 0),

    CONSTRAINT chk_shipping_rate_min_weight
        CHECK (min_weight >= 0),

    CONSTRAINT chk_shipping_rate_max_weight
        CHECK (
            max_weight IS NULL
            OR max_weight >= min_weight
        ),

    CONSTRAINT chk_shipping_rate_estimated_days
        CHECK (
            estimated_days_min > 0
            AND estimated_days_max >= estimated_days_min
        )
);

-- ============================================================
-- 5. RETURNS TABLE
-- ============================================================
CREATE TABLE returns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id UUID NOT NULL,
    order_id UUID NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(40) NOT NULL DEFAULT 'REQUESTED',
    tracking_number VARCHAR(100),
    carrier VARCHAR(100),
    pickup_address TEXT,
    pickup_scheduled_at TIMESTAMP,
    received_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_returns_shipment
        FOREIGN KEY (shipment_id)
        REFERENCES shipments(id)
        ON DELETE CASCADE
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_shipments_order_id ON shipments(order_id);
CREATE INDEX idx_shipments_user_id ON shipments(user_id);
CREATE INDEX idx_shipments_status ON shipments(shipment_status);
CREATE INDEX idx_shipments_carrier ON shipments(carrier);
CREATE INDEX idx_shipments_shipping_method ON shipments(shipping_method);
CREATE INDEX idx_shipments_created_at ON shipments(created_at);

CREATE INDEX idx_shipment_tracking_shipment_id ON shipment_tracking(shipment_id);
CREATE INDEX idx_shipment_tracking_status ON shipment_tracking(status);
CREATE INDEX idx_shipment_tracking_event_time ON shipment_tracking(event_time);
CREATE INDEX idx_shipment_tracking_activity_code ON shipment_tracking(activity_code);

CREATE INDEX idx_shipping_zones_country ON shipping_zones(country);
CREATE INDEX idx_shipping_zones_active ON shipping_zones(is_active);

CREATE INDEX idx_shipping_rates_zone_id ON shipping_rates(zone_id);
CREATE INDEX idx_shipping_rates_active ON shipping_rates(is_active);
CREATE INDEX idx_shipping_rates_carrier ON shipping_rates(carrier);
CREATE INDEX idx_shipping_rates_method ON shipping_rates(method_name);

CREATE INDEX idx_returns_shipment_id ON returns(shipment_id);
CREATE INDEX idx_returns_order_id ON returns(order_id);
CREATE INDEX idx_returns_status ON returns(status);
CREATE INDEX idx_returns_created_at ON returns(created_at);

-- ============================================================
-- TRIGGERS
-- ============================================================
CREATE OR REPLACE FUNCTION update_shipping_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_shipments_updated_at
BEFORE UPDATE ON shipments
FOR EACH ROW EXECUTE FUNCTION update_shipping_updated_at();

CREATE TRIGGER trg_returns_updated_at
BEFORE UPDATE ON returns
FOR EACH ROW EXECUTE FUNCTION update_shipping_updated_at();

-- ============================================================
-- SEED DATA (Dynamic & Idempotent Pattern)
-- ============================================================
INSERT INTO shipping_zones (zone_name, country, states, postal_codes, is_active)
VALUES 
    ('Central Zone', 'India', '["Karnataka", "Maharashtra"]'::jsonb, '["560100", "400001"]'::jsonb, TRUE),
    ('North Zone', 'India', '["Delhi", "Haryana"]'::jsonb, '["110001", "122001"]'::jsonb, TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO shipping_rates (zone_id, method_name, carrier, base_rate, per_kg_rate, min_weight, max_weight, estimated_days_min, estimated_days_max, is_active)
SELECT 
    sz.id, 'Standard Express', 'BlueDart Express', 50.00, 10.00, 0.000, 20.000, 2, 4, TRUE
FROM shipping_zones sz
WHERE sz.zone_name = 'Central Zone'
ON CONFLICT DO NOTHING;

INSERT INTO shipments (
    order_id, user_id, tracking_number, carrier, carrier_tracking_url, shipment_status,
    shipping_method, shipping_cost, estimated_weight, recipient_name, recipient_phone,
    street_address, city, state, country, pin_code, shipped_at, estimated_delivery
)
VALUES (
    'b1000000-0000-0000-0000-000000000001',
    '6b6d8b38-30c8-4b8f-bdce-e62386e8edf3',
    'TRK-EXPRESS-0001001',
    'BlueDart Express',
    'https://track.bluedart.com/TRK-EXPRESS-0001001',
    'IN_TRANSIT',
    'Standard Express',
    5.00,
    1.250,
    'Apurba Customer',
    '+919876543210',
    '123 Tech Park Road',
    'Bengaluru',
    'Karnataka',
    'India',
    '560100',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP + INTERVAL '2 days'
)
ON CONFLICT (tracking_number) DO NOTHING;

INSERT INTO shipment_tracking (shipment_id, status, location, description, activity_code, event_time)
SELECT 
    s.id, 'IN_TRANSIT', 'Bengaluru Hub', 'Package departed sorting facility', 'IN_TRANSIT', CURRENT_TIMESTAMP
FROM shipments s
WHERE s.tracking_number = 'TRK-EXPRESS-0001001'
ON CONFLICT DO NOTHING;
