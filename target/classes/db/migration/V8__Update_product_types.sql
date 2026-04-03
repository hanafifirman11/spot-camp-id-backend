-- Expand product types to support map rentals, rental items, and sales
ALTER TABLE products DROP CONSTRAINT IF EXISTS chk_product_type;
ALTER TABLE products DROP CONSTRAINT IF EXISTS chk_rental_fields;
ALTER TABLE products DROP CONSTRAINT IF EXISTS chk_sale_fields;

ALTER TABLE products
    ADD CONSTRAINT chk_product_type CHECK (type IN ('RENTAL_SPOT', 'RENTAL_ITEM', 'SALE'));

ALTER TABLE products
    ADD CONSTRAINT chk_rental_fields CHECK (
        (type IN ('RENTAL_SPOT', 'RENTAL_ITEM') AND stock_total IS NOT NULL AND daily_rate IS NOT NULL) OR
        (type NOT IN ('RENTAL_SPOT', 'RENTAL_ITEM') AND stock_total IS NULL AND daily_rate IS NULL)
    );

ALTER TABLE products
    ADD CONSTRAINT chk_sale_fields CHECK (
        (type = 'SALE' AND current_stock IS NOT NULL AND unit_price IS NOT NULL) OR
        (type != 'SALE' AND current_stock IS NULL AND unit_price IS NULL)
    );

UPDATE products
SET type = 'RENTAL_SPOT'
WHERE type = 'RENTAL';
