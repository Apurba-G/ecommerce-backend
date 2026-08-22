-- ============================================================
-- REVIEW DATABASE SCHEMA
-- Service: review-service
-- Database: review_db
-- Host Port: 5442
-- Internal PostgreSQL Port: 5432
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- 1. REVIEWS TABLE
-- ============================================================
CREATE TABLE reviews (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id          UUID NOT NULL,
    user_id             UUID NOT NULL,
    order_id            UUID,
    order_item_id       UUID,
    rating              INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title               VARCHAR(300),
    body                TEXT,
    verified_purchase   BOOLEAN NOT NULL DEFAULT FALSE,
    is_approved         BOOLEAN NOT NULL DEFAULT FALSE,
    is_featured         BOOLEAN NOT NULL DEFAULT FALSE,
    helpful_votes       INT NOT NULL DEFAULT 0,
    unhelpful_votes     INT NOT NULL DEFAULT 0,
    status              VARCHAR(30) NOT NULL DEFAULT 'APPROVED',
    approved_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_reviews_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'FLAGGED'))
);

-- ============================================================
-- 2. REVIEW IMAGES TABLE
-- ============================================================
CREATE TABLE review_images (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id           UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    image_url           TEXT NOT NULL,
    sort_order          INT NOT NULL DEFAULT 0,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 3. REVIEW VOTES TABLE
-- ============================================================
CREATE TABLE review_votes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id           UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    user_id             UUID NOT NULL,
    is_helpful          BOOLEAN NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_review_user_vote UNIQUE (review_id, user_id)
);

-- ============================================================
-- 4. REVIEW REPLIES TABLE
-- ============================================================
CREATE TABLE review_replies (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id           UUID NOT NULL UNIQUE REFERENCES reviews(id) ON DELETE CASCADE,
    seller_id           UUID NOT NULL,
    reply_text          TEXT NOT NULL,
    is_edited           BOOLEAN NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 5. REVIEW REPORTS TABLE
-- ============================================================
CREATE TABLE review_reports (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    review_id           UUID NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    reported_by         UUID NOT NULL,
    reason              VARCHAR(200) NOT NULL,
    description         TEXT,
    status              VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reviewed_by         UUID,
    reviewed_at         TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_review_report_status CHECK (status IN ('PENDING', 'REVIEWED', 'DISMISSED', 'ACTION_TAKEN')),
    CONSTRAINT uq_review_report_user UNIQUE (review_id, reported_by)
);

-- ============================================================
-- 6. PRODUCT RATING SUMMARY TABLE
-- ============================================================
CREATE TABLE product_rating_summary (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id          UUID UNIQUE NOT NULL,
    average_rating      DECIMAL(3, 2) NOT NULL DEFAULT 0.00,
    total_reviews       INT NOT NULL DEFAULT 0,
    one_star            INT NOT NULL DEFAULT 0,
    two_star            INT NOT NULL DEFAULT 0,
    three_star          INT NOT NULL DEFAULT 0,
    four_star           INT NOT NULL DEFAULT 0,
    five_star           INT NOT NULL DEFAULT 0,
    verified_reviews    INT NOT NULL DEFAULT 0,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- RATING SUMMARY TRIGGER FUNCTION
-- ============================================================
CREATE OR REPLACE FUNCTION update_product_rating_summary()
RETURNS TRIGGER AS $$
DECLARE
    v_product_id UUID;
BEGIN
    v_product_id := COALESCE(NEW.product_id, OLD.product_id);

    INSERT INTO product_rating_summary (
        product_id, average_rating, total_reviews,
        one_star, two_star, three_star, four_star, five_star,
        verified_reviews, updated_at
    )
    SELECT
        v_product_id,
        COALESCE(ROUND(AVG(rating)::NUMERIC, 2), 0.00),
        COUNT(*),
        COUNT(*) FILTER (WHERE rating = 1),
        COUNT(*) FILTER (WHERE rating = 2),
        COUNT(*) FILTER (WHERE rating = 3),
        COUNT(*) FILTER (WHERE rating = 4),
        COUNT(*) FILTER (WHERE rating = 5),
        COUNT(*) FILTER (WHERE verified_purchase = TRUE),
        CURRENT_TIMESTAMP
    FROM reviews
    WHERE product_id = v_product_id AND status = 'APPROVED'
    ON CONFLICT (product_id)
    DO UPDATE SET
        average_rating   = EXCLUDED.average_rating,
        total_reviews    = EXCLUDED.total_reviews,
        one_star         = EXCLUDED.one_star,
        two_star         = EXCLUDED.two_star,
        three_star       = EXCLUDED.three_star,
        four_star        = EXCLUDED.four_star,
        five_star        = EXCLUDED.five_star,
        verified_reviews = EXCLUDED.verified_reviews,
        updated_at       = EXCLUDED.updated_at;

    RETURN COALESCE(NEW, OLD);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_rating_summary
AFTER INSERT OR UPDATE OR DELETE ON reviews
FOR EACH ROW EXECUTE FUNCTION update_product_rating_summary();

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_reviews_product_id  ON reviews(product_id);
CREATE INDEX idx_reviews_user_id     ON reviews(user_id);
CREATE INDEX idx_reviews_order_item  ON reviews(order_item_id);
CREATE INDEX idx_reviews_status      ON reviews(status);
CREATE INDEX idx_reviews_rating      ON reviews(rating);
CREATE INDEX idx_reviews_verified    ON reviews(verified_purchase);
CREATE INDEX idx_reviews_prod_status ON reviews(product_id, status);
CREATE INDEX idx_review_votes_review ON review_votes(review_id);
CREATE INDEX idx_review_reports_rev  ON review_reports(review_id);
CREATE INDEX idx_review_reports_status ON review_reports(status);

-- ============================================================
-- SEED DATA (Idempotent Pattern)
-- ============================================================
INSERT INTO reviews (
    product_id, user_id, rating, title, body, verified_purchase, is_approved, status
) VALUES (
    'a1000000-0000-0000-0000-000000000001',
    '6b6d8b38-30c8-4b8f-bdce-e62386e8edf3',
    5,
    'Outstanding Performance!',
    'The build quality and battery life surpassed my expectations. Highly recommended!',
    TRUE,
    TRUE,
    'APPROVED'
) ON CONFLICT DO NOTHING;
