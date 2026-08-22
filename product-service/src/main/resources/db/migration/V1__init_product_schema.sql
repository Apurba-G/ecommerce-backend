-- ============================================================
-- PRODUCT DATABASE SCHEMA
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "unaccent";

-- ============================================================
-- CATEGORIES TABLE
-- ============================================================
CREATE TABLE categories (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    parent_id   UUID REFERENCES categories(id) ON DELETE SET NULL,
    name        VARCHAR(200) NOT NULL,
    slug        VARCHAR(250) UNIQUE NOT NULL,
    description TEXT,
    image_url   TEXT,
    banner_url  TEXT,
    icon_url    TEXT,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order  INT NOT NULL DEFAULT 0,
    level       INT NOT NULL DEFAULT 0,
    path        VARCHAR(500),
    meta_title  VARCHAR(300),
    meta_desc   VARCHAR(500),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON COLUMN categories.path IS 'Full path like: electronics/mobile-phones';
COMMENT ON COLUMN categories.level IS '0=root, 1=child, 2=grandchild';

-- ============================================================
-- BRANDS TABLE
-- ============================================================
CREATE TABLE brands (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(200) UNIQUE NOT NULL,
    slug        VARCHAR(250) UNIQUE NOT NULL,
    description TEXT,
    logo_url    TEXT,
    banner_url  TEXT,
    website_url TEXT,
    country     VARCHAR(100),
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    meta_title  VARCHAR(300),
    meta_desc   VARCHAR(500),
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- PRODUCTS TABLE
-- ============================================================
CREATE TABLE products (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id         UUID NOT NULL REFERENCES categories(id),
    brand_id            UUID REFERENCES brands(id) ON DELETE SET NULL,
    seller_id           UUID NOT NULL,
    name                VARCHAR(500) NOT NULL,
    slug                VARCHAR(600) UNIQUE NOT NULL,
    short_description   VARCHAR(1000),
    description         TEXT,
    sku                 VARCHAR(100) UNIQUE,
    barcode             VARCHAR(100),
    base_price          DECIMAL(10, 2) NOT NULL CHECK (base_price >= 0),
    selling_price       DECIMAL(10, 2) NOT NULL CHECK (selling_price >= 0),
    discount_percentage DECIMAL(5, 2) DEFAULT 0 CHECK (discount_percentage BETWEEN 0 AND 100),
    tax_percentage      DECIMAL(5, 2) DEFAULT 0 CHECK (tax_percentage >= 0),
    status              VARCHAR(30) NOT NULL DEFAULT 'DRAFT'
                            CHECK (status IN (
                                'DRAFT', 'ACTIVE', 'INACTIVE',
                                'OUT_OF_STOCK', 'DISCONTINUED'
                            )),
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    is_returnable       BOOLEAN NOT NULL DEFAULT TRUE,
    return_period_days  INT DEFAULT 7,
    weight              DOUBLE PRECISION DEFAULT 0,
    weight_unit         VARCHAR(10) DEFAULT 'KG'
                            CHECK (weight_unit IN ('KG', 'G', 'LB', 'OZ')),
    length              DOUBLE PRECISION DEFAULT 0,
    width               DOUBLE PRECISION DEFAULT 0,
    height              DOUBLE PRECISION DEFAULT 0,
    dimension_unit      VARCHAR(10) DEFAULT 'CM'
                            CHECK (dimension_unit IN ('CM', 'MM', 'IN', 'FT')),
    specifications      JSONB,
    tags                JSONB DEFAULT '[]',
    view_count          INT NOT NULL DEFAULT 0,
    purchase_count      INT NOT NULL DEFAULT 0,
    average_rating      DECIMAL(3, 2) DEFAULT 0.00,
    review_count        INT NOT NULL DEFAULT 0,
    meta_title          VARCHAR(300),
    meta_desc           VARCHAR(500),
    search_vector       TSVECTOR,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON COLUMN products.search_vector IS 'Full-text search vector';
COMMENT ON COLUMN products.specifications IS 'Key-value product specs as JSONB';

-- ============================================================
-- PRODUCT IMAGES TABLE
-- ============================================================
CREATE TABLE product_images (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url  TEXT NOT NULL,
    alt_text   VARCHAR(300),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================================
-- PRODUCT VARIANTS TABLE
-- ============================================================
CREATE TABLE product_variants (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id    UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name          VARCHAR(200) NOT NULL,
    sku           VARCHAR(100) UNIQUE,
    attributes    JSONB NOT NULL DEFAULT '{}',
    price         DECIMAL(10, 2) NOT NULL,
    selling_price DECIMAL(10, 2) NOT NULL,
    image_url     TEXT,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order    INT NOT NULL DEFAULT 0,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON COLUMN product_variants.attributes IS
    'Example: {"color": "Red", "size": "XL"}';

-- ============================================================
-- PRODUCT SPECIFICATIONS TABLE
-- ============================================================
CREATE TABLE product_specifications (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    spec_key   VARCHAR(200) NOT NULL,
    spec_value VARCHAR(500) NOT NULL,
    spec_group VARCHAR(100) DEFAULT 'General',
    sort_order INT NOT NULL DEFAULT 0
);

-- ============================================================
-- PRODUCT TAGS TABLE
-- ============================================================
CREATE TABLE product_tags (
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    tag        VARCHAR(100) NOT NULL,
    PRIMARY KEY (product_id, tag)
);

-- ============================================================
-- PRODUCT RELATED TABLE
-- ============================================================
CREATE TABLE product_related (
    product_id         UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    related_product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    relation_type      VARCHAR(30) DEFAULT 'SIMILAR'
                           CHECK (relation_type IN ('SIMILAR', 'ACCESSORY', 'BUNDLE', 'UPGRADE')),
    sort_order         INT DEFAULT 0,
    PRIMARY KEY (product_id, related_product_id)
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_categories_parent      ON categories(parent_id);
CREATE INDEX idx_categories_slug        ON categories(slug);
CREATE INDEX idx_categories_active      ON categories(is_active, sort_order);
CREATE INDEX idx_categories_featured    ON categories(is_featured) WHERE is_featured = TRUE;

CREATE INDEX idx_brands_slug            ON brands(slug);
CREATE INDEX idx_brands_active          ON brands(is_active);
CREATE INDEX idx_brands_featured        ON brands(is_featured) WHERE is_featured = TRUE;

CREATE INDEX idx_products_category      ON products(category_id);
CREATE INDEX idx_products_brand         ON products(brand_id);
CREATE INDEX idx_products_seller        ON products(seller_id);
CREATE INDEX idx_products_status        ON products(status);
CREATE INDEX idx_products_slug          ON products(slug);
CREATE INDEX idx_products_sku           ON products(sku);
CREATE INDEX idx_products_price         ON products(selling_price);
CREATE INDEX idx_products_featured      ON products(is_featured) WHERE is_featured = TRUE;
CREATE INDEX idx_products_active        ON products(is_active, status);
CREATE INDEX idx_products_rating        ON products(average_rating DESC);
CREATE INDEX idx_products_view_count    ON products(view_count DESC);
CREATE INDEX idx_products_search        ON products USING GIN (search_vector);
CREATE INDEX idx_products_tags          ON products USING GIN (tags);
CREATE INDEX idx_products_specs         ON products USING GIN (specifications);
CREATE INDEX idx_products_name_trgm     ON products USING GIN (name gin_trgm_ops);

CREATE INDEX idx_product_images_product ON product_images(product_id);
CREATE INDEX idx_product_images_primary ON product_images(product_id, is_primary);
CREATE INDEX idx_variants_product       ON product_variants(product_id);
CREATE INDEX idx_variants_sku           ON product_variants(sku);
CREATE INDEX idx_specs_product          ON product_specifications(product_id, spec_group);

-- ============================================================
-- FULL-TEXT SEARCH TRIGGER & UPDATED_AT TRIGGER
-- ============================================================
CREATE OR REPLACE FUNCTION update_product_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', COALESCE(NEW.name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.short_description, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'C') ||
        setweight(to_tsvector('english', COALESCE(NEW.sku, '')), 'A');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_products_search_vector
    BEFORE INSERT OR UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_product_search_vector();

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ============================================================
-- SEED DATA
-- ============================================================
INSERT INTO categories (name, slug, level, path, is_active, sort_order) VALUES
    ('Electronics', 'electronics', 0, 'electronics', true, 1),
    ('Fashion', 'fashion', 0, 'fashion', true, 2),
    ('Home & Living', 'home-living', 0, 'home-living', true, 3),
    ('Sports', 'sports', 0, 'sports', true, 4),
    ('Books', 'books', 0, 'books', true, 5),
    ('Beauty', 'beauty', 0, 'beauty', true, 6),
    ('Toys', 'toys', 0, 'toys', true, 7),
    ('Automotive', 'automotive', 0, 'automotive', true, 8)
ON CONFLICT (slug) DO NOTHING;

INSERT INTO categories (parent_id, name, slug, level, path, is_active)
    SELECT id, 'Mobile Phones', 'mobile-phones', 1, 'electronics/mobile-phones', true
    FROM categories WHERE slug = 'electronics'
ON CONFLICT (slug) DO NOTHING;

INSERT INTO categories (parent_id, name, slug, level, path, is_active)
    SELECT id, 'Laptops', 'laptops', 1, 'electronics/laptops', true
    FROM categories WHERE slug = 'electronics'
ON CONFLICT (slug) DO NOTHING;

INSERT INTO categories (parent_id, name, slug, level, path, is_active)
    SELECT id, 'Smart TVs', 'smart-tvs', 1, 'electronics/smart-tvs', true
    FROM categories WHERE slug = 'electronics'
ON CONFLICT (slug) DO NOTHING;

INSERT INTO categories (parent_id, name, slug, level, path, is_active)
    SELECT id, 'Men', 'men', 1, 'fashion/men', true
    FROM categories WHERE slug = 'fashion'
ON CONFLICT (slug) DO NOTHING;

INSERT INTO categories (parent_id, name, slug, level, path, is_active)
    SELECT id, 'Women', 'women', 1, 'fashion/women', true
    FROM categories WHERE slug = 'fashion'
ON CONFLICT (slug) DO NOTHING;

INSERT INTO brands (name, slug, is_active, is_featured) VALUES
    ('Apple', 'apple', true, true),
    ('Samsung', 'samsung', true, true),
    ('OnePlus', 'oneplus', true, true),
    ('Nike', 'nike', true, true),
    ('Adidas', 'adidas', true, true),
    ('Sony', 'sony', true, false),
    ('LG', 'lg', true, false),
    ('Dell', 'dell', true, false),
    ('HP', 'hp', true, false),
    ('Lenovo', 'lenovo', true, false),
    ('Boat', 'boat', true, true),
    ('Noise', 'noise', true, false)
ON CONFLICT (slug) DO NOTHING;
