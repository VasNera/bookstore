INSERT INTO roles (name)
VALUES
    ('ADMIN'),
    ('CUSTOMER');

INSERT INTO capabilities (name, description)
VALUES
    ('VIEW_PRODUCTS', 'View products'),
    ('VIEW_CATEGORIES', 'View categories'),
    ('VIEW_REVIEWS', 'View reviews'),
    ('MANAGE_PRODUCTS', 'Create, update and delete products'),
    ('MANAGE_CATEGORIES', 'Create, update and delete categories'),
    ('MANAGE_USERS', 'Manage users'),
    ('MANAGE_ORDERS', 'Manage all orders'),
    ('CREATE_ORDER', 'Create orders'),
    ('MANAGE_OWN_CART', 'Manage own shopping cart'),
    ('CREATE_REVIEW', 'Create product reviews'),
    ('MANAGE_OWN_REVIEWS', 'Manage own reviews'),
    ('VIEW_OWN_ORDERS', 'View own orders'),
    ('CANCEL_OWN_ORDERS', 'Cancel own orders');

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
CROSS JOIN capabilities c
WHERE r.name = 'ADMIN';

INSERT INTO roles_capabilities (role_id, capability_id)
SELECT r.id, c.id
FROM roles r
JOIN capabilities c
    ON c.name IN (
        'VIEW_PRODUCTS',
        'VIEW_CATEGORIES',
        'VIEW_REVIEWS',
        'CREATE_ORDER',
        'MANAGE_OWN_CART',
        'CREATE_REVIEW',
        'MANAGE_OWN_REVIEWS',
        'VIEW_OWN_ORDERS',
        'CANCEL_OWN_ORDERS'
    )
WHERE r.name = 'CUSTOMER';