DROP SCHEMA IF EXISTS "restaurant" CASCADE;

CREATE SCHEMA "restaurant";

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TYPE IF EXISTS approval_status;
CREATE TYPE approval_status AS ENUM ('APPROVED','REJECTED');

DROP TABLE IF EXISTS "restaurant".order_approval CASCADE;

CREATE TABLE "restaurant".order_approval
(
    id uuid NOT NULL,
    restaurant_id uuid NOT NULL,
    order_id uuid NOT NULL,
    approval_status approval_status NOT NULL,
    CONSTRAINT order_approval_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_order_approval_restaurant_id ON "restaurant".order_approval(restaurant_id);
CREATE INDEX idx_order_approval_order_id ON "restaurant".order_approval(order_id);

DROP TABLE IF EXISTS "restaurant".restaurants CASCADE;

CREATE TABLE "restaurant".restaurants
(
    id uuid NOT NULL,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT restaurants_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "restaurant".restaurant_products CASCADE;

CREATE TABLE "restaurant".restaurant_products
(
    id uuid NOT NULL,
    restaurant_id uuid NOT NULL,
    product_id uuid NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    price numeric(10,2) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT restaurant_products_pkey PRIMARY KEY (id),
    CONSTRAINT uk_restaurant_product UNIQUE (restaurant_id, product_id),
    CONSTRAINT fk_restaurant_products_restaurant FOREIGN KEY (restaurant_id) 
        REFERENCES "restaurant".restaurants(id) ON DELETE CASCADE
);

CREATE INDEX idx_restaurant_products_restaurant_id 
    ON "restaurant".restaurant_products(restaurant_id);

DROP MATERIALIZED VIEW IF EXISTS "restaurant".order_restaurant_m_view CASCADE;

CREATE MATERIALIZED VIEW "restaurant".order_restaurant_m_view AS
SELECT 
    r.id AS restaurant_id,
    r.name AS restaurant_name,
    r.active AS restaurant_active,
    p.product_id AS product_id,
    p.product_name AS product_name,
    p.price AS product_price,
    p.available AS product_available
FROM "restaurant".restaurants r
INNER JOIN "restaurant".restaurant_products p ON r.id = p.restaurant_id;

CREATE UNIQUE INDEX idx_order_restaurant_m_view 
    ON "restaurant".order_restaurant_m_view(restaurant_id, product_id);

REFRESH MATERIALIZED VIEW "restaurant".order_restaurant_m_view;
