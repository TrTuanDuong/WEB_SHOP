ALTER TABLE shop_product
ADD COLUMN image CHARACTER VARYING;

UPDATE shop_product
SET image = 'image-1.jpg'
WHERE name LIKE '%Áo khoác nữ%'

UPDATE shop_product
SET image = 'image-2.jpg'
WHERE name LIKE '%Áo thun nam%'

UPDATE shop_product
SET image = 'image-3.jpg'
WHERE name LIKE '%Áo thun nữ%'

UPDATE shop_product
SET image = 'image-4.jpg'
WHERE name LIKE '%Váy nữ%'

UPDATE shop_product
SET image = 'image-5.jpg'
WHERE name LIKE '%Áo kiểu nữ%'

UPDATE shop_product
SET image = 'image-6.jpg'
WHERE name LIKE '%Chân váy%'

UPDATE shop_product
SET image = 'image-7.jpg'
WHERE name LIKE '%Quần jean nữ%'

UPDATE shop_product
SET image = 'image-8.jpg'
WHERE name LIKE '%Áo sơ mi nam%'

UPDATE shop_product
SET image = 'image-9.jpg'
WHERE name LIKE '%Quần jean nam%'

UPDATE shop_product
SET image = 'image-10.jpg'
WHERE name LIKE '%Quần kaki nam%'

UPDATE shop_product
SET image = 'image-11.jpg'
WHERE name LIKE '%Áo polo nam%'

UPDATE shop_product
SET image = 'image-12.jpg'
WHERE name LIKE '%Áo khoác nam%'

UPDATE shop_product
SET image = 'image-13.jpg'
WHERE name LIKE '%Quần short bé trai%'

UPDATE shop_product
SET image = 'image-14.jpg'
WHERE name LIKE '%Áo sơ mi bé trai%'

UPDATE shop_product
SET image = 'image-15.jpg'
WHERE name LIKE '%Set đồ bé trai%'

UPDATE shop_product
SET image = 'image-16.jpg'
WHERE name LIKE '%Quần jean bé trai%'

UPDATE shop_product
SET image = 'image-17.jpg'
WHERE name LIKE '%Áo khoác bé trai%'

UPDATE shop_product
SET image = 'image-18.jpg'
WHERE name LIKE '%Áo kiểu bé gái%'

UPDATE shop_product
SET image = 'image-19.jpg'
WHERE name LIKE '%Set đồ bé gái%'

UPDATE shop_product
SET image = 'image-20.jpg'
WHERE name LIKE '%Set đồ bé gái%'

UPDATE shop_product
SET image = 'image-21.jpg'
WHERE name LIKE '%Quần legging bé gái%'

UPDATE shop_product
SET image = 'image-22.jpg'
WHERE name LIKE '%Áo khoác bé gái%'