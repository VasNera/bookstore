CREATE TABLE carts(
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,

    CONSTRAINT pk_carts
        PRIMARY KEY (id),

    CONSTRAINT uk_carts_user_id
        UNIQUE (user_id),

    CONSTRAINT fk_carts_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE cart_items(
    id BIGINT NOT NULL AUTO_INCREMENT,
    quantity INT NOT NULL,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    CONSTRAINT pk_cart_items
        PRIMARY KEY (id),

    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id)
        REFERENCES carts(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_cart_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE RESTRICT,

    CONSTRAINT uk_cart_items_cart_product
        UNIQUE (cart_id, product_id),

    INDEX ix_cart_items_product_id (product_id)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE orders(
    id BIGINT NOT NULL AUTO_INCREMENT,

    document_type VARCHAR(255) NOT NULL,
    payment_method VARCHAR(255) NOT NULL,
    payment_status VARCHAR(255) NOT NULL,
    order_status VARCHAR(255) NOT NULL,

    order_number VARCHAR(255) NOT NULL,

    billing_business_name VARCHAR(255),
    billing_tax_number VARCHAR(255),
    billing_street VARCHAR(255),
    billing_profession VARCHAR(255),
    billing_tax_office VARCHAR(255),

    shipping_street VARCHAR(255) NOT NULL,
    shipping_city VARCHAR(255) NOT NULL,
    shipping_postal_code VARCHAR(255) NOT NULL,

    notes TEXT,

    total_amount DECIMAL(10,2) NOT NULL,

    user_id BIGINT NOT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME NULL,

    CONSTRAINT pk_orders
        PRIMARY KEY (id),

    CONSTRAINT uk_orders_order_number
        UNIQUE (order_number),

    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT,

    INDEX ix_orders_user_id (user_id),
    INDEX ix_orders_order_status (order_status),
    INDEX ix_orders_payment_status (payment_status),
    INDEX ix_orders_deleted (deleted)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE order_items(
    id BIGINT NOT NULL AUTO_INCREMENT,

    price_at_purchase DECIMAL(10,2) NOT NULL,
    product_title VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,

    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    CONSTRAINT pk_order_items
        PRIMARY KEY (id),

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE RESTRICT,

    INDEX ix_order_items_order_id (order_id),
    INDEX ix_order_items_product_id (product_id)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE reviews(
    id BIGINT NOT NULL AUTO_INCREMENT,

    comment TEXT,
    rating INT NOT NULL,

    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,

    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    deleted_at DATETIME NULL,

    CONSTRAINT pk_reviews
        PRIMARY KEY (id),

    CONSTRAINT uk_reviews_user_product
        UNIQUE (user_id, product_id),

    CONSTRAINT fk_reviews_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_reviews_product
        FOREIGN KEY (product_id)
        REFERENCES products(id)
        ON DELETE CASCADE,

    INDEX ix_reviews_product_id (product_id)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;