CREATE TABLE roles(
id BIGINT NOT NULL AUTO_INCREMENT,
name VARCHAR(255) NOT NULL,

CONSTRAINT pk_roles PRIMARY KEY(id),
CONSTRAINT uk_roles_name UNIQUE(name)

) ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE capabilities(
id BIGINT NOT NULL AUTO_INCREMENT,
name VARCHAR(255) NOT NULL,

CONSTRAINT pk_capabilities PRIMARY KEY(id),
CONSTRAINT uk_capabilities_name UNIQUE(name)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;

  CREATE TABLE roles_capabilities(
  role_id BIGINT NOT NULL,
  capability_id BIGINT NOT NULL,

  CONSTRAINT pk_roles_capabilities
  PRIMARY KEY(role_id, capability_id),

  CONSTRAINT fk_roles_capabilities_role
  FOREIGN KEY (role_id)
  REFERENCES roles(id)
  ON DELETE CASCADE,

  CONSTRAINT fk_roles_capabilities_capability
  FOREIGN KEY (capability_id)
  REFERENCES capabilities(id)
  ON DELETE CASCADE,

  INDEX ix_roles_capabilities_capability_id (capability_id)

  ) ENGINE=InnoDB
        DEFAULT CHARSET=utf8mb4
        COLLATE=utf8mb4_0900_ai_ci;

  CREATE TABLE users(
  id BIGINT NOT NULL AUTO_INCREMENT,
  uuid BINARY(16) NOT NULL,
  username VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  firstname VARCHAR(50) NOT NULL,
  lastname VARCHAR(50) NOT NULL,
  email VARCHAR(255) NOT NULL,
  phone VARCHAR(25) NOT NULL,
  role_id BIGINT NOT NULL,

   created_at DATETIME NOT NULL,
   updated_at DATETIME NOT NULL,
   deleted TINYINT(1) NOT NULL DEFAULT 0,
   deleted_at DATETIME NULL,

   CONSTRAINT pk_users PRIMARY KEY (id),

    CONSTRAINT uk_users_uuid UNIQUE (uuid),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_username UNIQUE (username),

    CONSTRAINT fk_users_role
    FOREIGN KEY (role_id)
    REFERENCES roles(id)
    ON DELETE RESTRICT,

    INDEX ix_users_role_id (role_id),
    INDEX ix_users_deleted (deleted),
    INDEX ix_users_deleted_at (deleted_at)
    ) ENGINE=InnoDB
         DEFAULT CHARSET=utf8mb4
         COLLATE=utf8mb4_0900_ai_ci;

    CREATE TABLE categories(
        id BIGINT NOT NULL AUTO_INCREMENT,
        name VARCHAR(255) NOT NULL,
        parent_category_id BIGINT NULL,

        CONSTRAINT pk_categories
            PRIMARY KEY (id),

        CONSTRAINT uk_categories_name
            UNIQUE (name),

        CONSTRAINT fk_categories_parent
            FOREIGN KEY (parent_category_id)
            REFERENCES categories(id)
            ON DELETE RESTRICT,

        INDEX ix_categories_parent_category_id (parent_category_id)

    ) ENGINE=InnoDB
      DEFAULT CHARSET=utf8mb4
      COLLATE=utf8mb4_0900_ai_ci;

  CREATE TABLE products(
     id BIGINT NOT NULL AUTO_INCREMENT,
     title VARCHAR(255) NOT NULL,
     sku VARCHAR(255) NOT NULL,

     dimensions VARCHAR(255),
     price DECIMAL(10,2) NOT NULL,
     discount_price DECIMAL(10,2),
     stock INT NOT NULL,
     image_url VARCHAR(500),
     featured TINYINT(1) NOT NULL DEFAULT 0,
     version BIGINT NOT NULL,
     description TEXT,
     category_id BIGINT NOT NULL,

      created_at DATETIME NOT NULL,
      updated_at DATETIME NOT NULL,
      deleted TINYINT(1) NOT NULL DEFAULT 0,
      deleted_at DATETIME NULL,

      CONSTRAINT pk_products
      PRIMARY KEY (id),

      CONSTRAINT uk_products_sku
      UNIQUE (sku),

      CONSTRAINT fk_products_category
      FOREIGN KEY (category_id)
      REFERENCES categories(id)
      ON DELETE RESTRICT,

      INDEX ix_products_category_id (category_id),
      INDEX ix_products_featured (featured),
      INDEX ix_products_deleted (deleted)

      ) ENGINE=InnoDB
        DEFAULT CHARSET=utf8mb4
        COLLATE=utf8mb4_0900_ai_ci;

    CREATE TABLE books(
    id BIGINT NOT NULL,
    isbn VARCHAR(255) NOT NULL,
    pages INT NULL,
    author VARCHAR(255),
    release_year INT,
    publisher VARCHAR(255),

     CONSTRAINT pk_books
     PRIMARY KEY (id),

     CONSTRAINT uk_books_isbn
     UNIQUE (isbn),

     CONSTRAINT fk_books_product
     FOREIGN KEY (id)
     REFERENCES products(id)
     ON DELETE CASCADE

     ) ENGINE=InnoDB
         DEFAULT CHARSET=utf8mb4
         COLLATE=utf8mb4_0900_ai_ci;

    CREATE TABLE music(
    id BIGINT NOT NULL,
    production_company VARCHAR(255),
    artist VARCHAR(255),

    CONSTRAINT pk_music
    PRIMARY KEY (id),

    CONSTRAINT fk_music_product
    FOREIGN KEY (id)
    REFERENCES products(id)
    ON DELETE CASCADE

    ) ENGINE=InnoDB
        DEFAULT CHARSET=utf8mb4
        COLLATE=utf8mb4_0900_ai_ci;

     CREATE TABLE stationery(
     id BIGINT NOT NULL,
     company VARCHAR(255),

     CONSTRAINT pk_stationery
     PRIMARY KEY (id),

     CONSTRAINT fk_stationery_product
     FOREIGN KEY (id)
     REFERENCES products(id)
     ON DELETE CASCADE

      ) ENGINE=InnoDB
          DEFAULT CHARSET=utf8mb4
          COLLATE=utf8mb4_0900_ai_ci;

     CREATE TABLE toys(
     id BIGINT NOT NULL,

     CONSTRAINT pk_toys
     PRIMARY KEY (id),


     CONSTRAINT fk_toys_product
     FOREIGN KEY (id)
     REFERENCES products(id)
     ON DELETE CASCADE

     ) ENGINE=InnoDB
         DEFAULT CHARSET=utf8mb4
         COLLATE=utf8mb4_0900_ai_ci;

     CREATE TABLE user_favourites(
     user_id BIGINT NOT NULL,
     product_id BIGINT NOT NULL,

     CONSTRAINT pk_user_favourites
     PRIMARY KEY (user_id, product_id),

     CONSTRAINT fk_user_favourites_user
     FOREIGN KEY (user_id)
     REFERENCES users(id)
     ON DELETE CASCADE,

     CONSTRAINT fk_user_favourites_product
     FOREIGN KEY (product_id)
     REFERENCES products(id)
     ON DELETE CASCADE,

     INDEX ix_user_favourites_product_id (product_id)

     ) ENGINE=InnoDB
           DEFAULT CHARSET=utf8mb4
           COLLATE=utf8mb4_0900_ai_ci;

