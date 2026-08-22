-- ============================================================
-- WISHLIST DATABASE
-- Service: wishlist-service
-- Database: wishlist_db
-- Host Port: 5437
-- Internal PostgreSQL Port: 5432
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. WISHLISTS
-- ============================================================
CREATE TABLE wishlists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_public BOOLEAN NOT NULL DEFAULT FALSE,
    share_token VARCHAR(255) UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_wishlist_name_not_blank
        CHECK (length(trim(name)) > 0),

    CONSTRAINT uq_user_wishlist_name
        UNIQUE (user_id, name)
);

-- ============================================================
-- 2. WISHLIST ITEMS
-- ============================================================
CREATE TABLE wishlist_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wishlist_id UUID NOT NULL,
    product_id UUID NOT NULL,
    variant_id UUID,
    product_name VARCHAR(255) NOT NULL,
    product_image TEXT,
    price DECIMAL(19, 2) NOT NULL,
    in_stock BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wishlist_items_wishlist
        FOREIGN KEY (wishlist_id)
        REFERENCES wishlists(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_wishlist_item_name_not_blank
        CHECK (length(trim(product_name)) > 0),

    CONSTRAINT chk_wishlist_item_price_non_negative
        CHECK (price >= 0)
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_wishlists_user_id ON wishlists(user_id);
CREATE INDEX idx_wishlists_public ON wishlists(is_public);
CREATE INDEX idx_wishlists_created_at ON wishlists(created_at);

CREATE INDEX idx_wishlist_items_wishlist_id ON wishlist_items(wishlist_id);
CREATE INDEX idx_wishlist_items_product_id ON wishlist_items(product_id);
CREATE INDEX idx_wishlist_items_variant_id ON wishlist_items(variant_id);
CREATE INDEX idx_wishlist_items_created_at ON wishlist_items(created_at);

CREATE UNIQUE INDEX uq_wishlists_one_default_per_user
    ON wishlists(user_id)
    WHERE is_default = TRUE;

CREATE UNIQUE INDEX uq_wishlist_product_without_variant
    ON wishlist_items(wishlist_id, product_id)
    WHERE variant_id IS NULL;

CREATE UNIQUE INDEX uq_wishlist_product_with_variant
    ON wishlist_items(wishlist_id, product_id, variant_id)
    WHERE variant_id IS NOT NULL;

-- ============================================================
-- TRIGGERS
-- ============================================================
CREATE OR REPLACE FUNCTION update_wishlist_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_wishlists_updated_at
BEFORE UPDATE ON wishlists
FOR EACH ROW EXECUTE FUNCTION update_wishlist_updated_at();

-- ============================================================
-- SEED DATA (Phase-1 Dynamic & Idempotent Pattern)
-- ============================================================

INSERT INTO wishlists (user_id, name, is_default, is_public, share_token)
VALUES
    ('6b6d8b38-30c8-4b8f-bdce-e62386e8edf3', 'My Favorites', TRUE, FALSE, NULL),
    ('6b6d8b38-30c8-4b8f-bdce-e62386e8edf3', 'Birthday Wishlist', FALSE, TRUE, 'bday-gifts-2026-demo')
ON CONFLICT (user_id, name) DO NOTHING;

INSERT INTO wishlist_items (wishlist_id, product_id, variant_id, product_name, product_image, price, in_stock)
SELECT
    w.id,
    'e4b2d56a-1234-4567-890a-bcdef1234567',
    NULL,
    'Apple iPhone 15 Pro Max 256GB',
    'https://images.unsplash.com/photo-1695048133142-1a20484d2569',
    1099.00,
    TRUE
FROM wishlists w
WHERE w.name = 'My Favorites' AND w.user_id = '6b6d8b38-30c8-4b8f-bdce-e62386e8edf3'
ON CONFLICT DO NOTHING;

INSERT INTO wishlist_items (wishlist_id, product_id, variant_id, product_name, product_image, price, in_stock)
SELECT
    w.id,
    'f5c3e67b-2345-5678-901b-cdef01234568',
    NULL,
    'Apple MacBook Pro 16 M3 Max',
    'https://images.unsplash.com/photo-1517336714731-489689fd1ca8',
    3299.00,
    TRUE
FROM wishlists w
WHERE w.name = 'Birthday Wishlist' AND w.user_id = '6b6d8b38-30c8-4b8f-bdce-e62386e8edf3'
ON CONFLICT DO NOTHING;
