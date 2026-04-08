CREATE TABLE IF NOT EXISTS clothing_product (
    product_code TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    size TEXT NOT NULL,
    color TEXT NOT NULL,
    price NUMERIC(14, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS shop_product (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    group_name TEXT NOT NULL,
    segment TEXT NOT NULL,
    size TEXT NOT NULL,
    color TEXT NOT NULL,
    price NUMERIC(14, 2) NOT NULL
);

WITH group_defs AS (
    SELECT *
    FROM (
        VALUES
            ('AD-M', 'Người lớn', 'Nam', 1,
             ARRAY['Áo thun nam', 'Áo sơ mi nam', 'Quần jean nam', 'Quần kaki nam', 'Áo polo nam', 'Áo khoác nam']::TEXT[]),
            ('AD-F', 'Người lớn', 'Nữ', 19,
             ARRAY['Áo thun nữ', 'Áo kiểu nữ', 'Váy nữ', 'Quần jean nữ', 'Áo khoác nữ', 'Chân váy']::TEXT[]),
            ('KD-B', 'Trẻ em', 'Bé trai', 37,
             ARRAY['Áo thun bé trai', 'Quần short bé trai', 'Áo sơ mi bé trai', 'Set đồ bé trai', 'Quần jean bé trai', 'Áo khoác bé trai']::TEXT[]),
            ('KD-G', 'Trẻ em', 'Bé gái', 55,
             ARRAY['Váy bé gái', 'Áo kiểu bé gái', 'Set đồ bé gái', 'Quần legging bé gái', 'Áo khoác bé gái', 'Áo thun bé gái']::TEXT[])
    ) AS t(prefix, group_name, segment, start_no, name_bases)
), generated AS (
    SELECT
        format('%s-%03s', gd.prefix, gd.start_no + n - 1) AS id,
        gd.name_bases[((n - 1) % array_length(gd.name_bases, 1)) + 1] || ' ' || n AS name,
        gd.group_name,
        gd.segment,
        (ARRAY['S', 'M', 'L', 'XL'])[((n - 1) % 4) + 1] AS size,
        (ARRAY['Đen', 'Trắng', 'Xanh', 'Be', 'Nâu', 'Hồng'])[((n - 1) % 6) + 1] AS color,
        (179000 + ((n - 1) % 9) * 35000)::NUMERIC(14, 2) AS price
    FROM group_defs gd
    CROSS JOIN generate_series(1, 18) AS n
)
INSERT INTO shop_product (id, name, group_name, segment, size, color, price)
SELECT id, name, group_name, segment, size, color, price
FROM generated
WHERE NOT EXISTS (SELECT 1 FROM shop_product);
