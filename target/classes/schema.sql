CREATE TABLE IF NOT EXISTS clothing_product (
    product_code TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    size TEXT NOT NULL,
    color TEXT NOT NULL,
    price NUMERIC(14, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0
);
