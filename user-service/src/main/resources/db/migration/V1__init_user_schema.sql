-- ============================================================
-- USER DATABASE
-- ============================================================

-- ============================================================
-- EXTENSIONS
-- ============================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- USER PROFILES
-- ============================================================
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Logical reference to AUTH_DB.users.id.
    -- No cross-database foreign key.
    user_id UUID NOT NULL UNIQUE,

    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(30),
    profile_image TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- ADDRESSES
-- ============================================================
CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Logical reference to AUTH_DB.users.id
    user_id UUID NOT NULL,

    address_type VARCHAR(50),
    full_name VARCHAR(150),
    phone VARCHAR(30),

    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),

    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),

    is_default BOOLEAN NOT NULL DEFAULT FALSE,

    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_addresses_user_profile
        FOREIGN KEY (user_id)
        REFERENCES user_profiles(user_id)
        ON DELETE CASCADE
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_user_profiles_user_id
    ON user_profiles(user_id);

CREATE INDEX idx_addresses_user_id
    ON addresses(user_id);

CREATE INDEX idx_addresses_user_default
    ON addresses(user_id, is_default);

-- ============================================================
-- DEFAULT ADDRESS (Ensures only one default address per user)
-- ============================================================
CREATE UNIQUE INDEX uq_one_default_address_per_user
    ON addresses(user_id)
    WHERE is_default = TRUE;

-- ============================================================
-- UPDATED_AT TRIGGER FUNCTION
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- USER PROFILE UPDATED_AT TRIGGER
-- ============================================================
CREATE TRIGGER trg_user_profiles_updated_at
BEFORE UPDATE ON user_profiles
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- ADDRESS UPDATED_AT TRIGGER
-- ============================================================
CREATE TRIGGER trg_addresses_updated_at
BEFORE UPDATE ON addresses
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();
