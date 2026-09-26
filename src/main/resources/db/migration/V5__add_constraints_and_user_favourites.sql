ALTER TABLE reviews
ADD CONSTRAINT chk_reviews_rating
CHECK (rating BETWEEN 1 AND 5);


ALTER TABLE cart_items
ADD CONSTRAINT chk_cart_items_quantity
CHECK (quantity > 0);


ALTER TABLE order_items
ADD CONSTRAINT chk_order_items_quantity
CHECK (quantity > 0);


ALTER TABLE order_items
ADD CONSTRAINT uk_order_items_order_product
UNIQUE (order_id, product_id);


ALTER TABLE orders
ADD CONSTRAINT chk_orders_total_amount
CHECK (total_amount >= 0);


