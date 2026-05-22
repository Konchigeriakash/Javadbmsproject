USE project;

-- 1. Show all suppliers.
SELECT supplier_id, name, contact_name, phone, city
FROM Suppliers
ORDER BY supplier_id;

-- 2. Show all products with current stock.
SELECT product_id, name, unit_price, quantity, reorder_level
FROM vw_ProductInventory
ORDER BY product_id;

-- 3. Find products supplied by TechSource India.
SELECT p.product_id, p.name, p.unit_price
FROM Products p
JOIN Suppliers s ON p.supplier_id = s.supplier_id
WHERE s.name = 'TechSource India'
ORDER BY p.name;

-- 4. Show products whose price is greater than 1000.
SELECT product_id, name, unit_price
FROM Products
WHERE unit_price > 1000
ORDER BY unit_price DESC;

-- 5. Insert a new product and starting inventory.
INSERT INTO Products(name, unit_price, reorder_level, supplier_id)
VALUES ('HDMI Cable', 299.00, 20, 1);

INSERT INTO Inventory(product_id, quantity)
VALUES (LAST_INSERT_ID(), 60);

-- 6. Update product price.
UPDATE Products
SET unit_price = 325.00
WHERE name = 'HDMI Cable';

-- 7. Adjust current stock.
UPDATE Inventory i
JOIN Products p ON i.product_id = p.product_id
SET i.quantity = i.quantity + 10
WHERE p.name = 'HDMI Cable';

-- 8. Delete the demo product if it has not been used in an order.
DELETE i
FROM Inventory i
JOIN Products p ON i.product_id = p.product_id
WHERE p.name = 'HDMI Cable';

DELETE FROM Products
WHERE name = 'HDMI Cable';
