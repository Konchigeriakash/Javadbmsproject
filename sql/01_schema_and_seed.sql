-- Inventory Management System database setup for MySQL.
-- Run this as a MySQL admin user. It recreates the project database.

DROP DATABASE IF EXISTS project;
CREATE DATABASE project;
USE project;

CREATE USER IF NOT EXISTS 'javauser'@'localhost' IDENTIFIED BY 'password123';
GRANT ALL PRIVILEGES ON project.* TO 'javauser'@'localhost';

CREATE TABLE Suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_name VARCHAR(100),
    phone VARCHAR(30),
    email VARCHAR(120),
    city VARCHAR(80),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_suppliers_email (email)
);

CREATE TABLE Products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    reorder_level INT NOT NULL DEFAULT 0,
    supplier_id INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_supplier
        FOREIGN KEY (supplier_id) REFERENCES Suppliers(supplier_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_products_price CHECK (unit_price >= 0),
    CONSTRAINT chk_products_reorder CHECK (reorder_level >= 0)
);

CREATE TABLE Inventory (
    product_id INT PRIMARY KEY,
    quantity INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id) REFERENCES Products(product_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT chk_inventory_quantity CHECK (quantity >= 0)
);

CREATE TABLE Orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    order_date DATE NOT NULL,
    status ENUM('Processing', 'Completed', 'Cancelled') NOT NULL DEFAULT 'Processing',
    customer_name VARCHAR(120) NOT NULL
);

CREATE TABLE OrderItems (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES Orders(order_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id) REFERENCES Products(product_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT chk_order_items_price CHECK (unit_price >= 0)
);

CREATE TABLE StockMovements (
    movement_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    movement_type ENUM('IN', 'OUT', 'ADJUSTMENT') NOT NULL,
    quantity_change INT NOT NULL,
    reason VARCHAR(200) NOT NULL,
    reference_order_id INT NULL,
    movement_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_movements_product
        FOREIGN KEY (product_id) REFERENCES Products(product_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_stock_movements_order
        FOREIGN KEY (reference_order_id) REFERENCES Orders(order_id)
        ON UPDATE CASCADE ON DELETE SET NULL
);

DELIMITER //

CREATE TRIGGER trg_inventory_before_update
BEFORE UPDATE ON Inventory
FOR EACH ROW
BEGIN
    IF NEW.quantity < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Inventory quantity cannot be negative';
    END IF;
END//

CREATE TRIGGER trg_order_item_before_insert
BEFORE INSERT ON OrderItems
FOR EACH ROW
BEGIN
    DECLARE current_stock INT DEFAULT -1;

    IF NEW.quantity <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Order item quantity must be positive';
    END IF;

    SELECT COALESCE((SELECT quantity FROM Inventory WHERE product_id = NEW.product_id), -1)
    INTO current_stock;

    IF current_stock < 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Inventory record does not exist for this product';
    ELSEIF current_stock < NEW.quantity THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Insufficient stock for this order item';
    END IF;
END//

CREATE TRIGGER trg_order_item_after_insert
AFTER INSERT ON OrderItems
FOR EACH ROW
BEGIN
    UPDATE Inventory
    SET quantity = quantity - NEW.quantity
    WHERE product_id = NEW.product_id;

    INSERT INTO StockMovements(product_id, movement_type, quantity_change, reason, reference_order_id)
    VALUES (NEW.product_id, 'OUT', -NEW.quantity, CONCAT('Order #', NEW.order_id), NEW.order_id);
END//

CREATE TRIGGER trg_orders_after_cancel
AFTER UPDATE ON Orders
FOR EACH ROW
BEGIN
    IF OLD.status <> 'Cancelled' AND NEW.status = 'Cancelled' THEN
        UPDATE Inventory i
        JOIN OrderItems oi ON i.product_id = oi.product_id
        SET i.quantity = i.quantity + oi.quantity
        WHERE oi.order_id = NEW.order_id;

        INSERT INTO StockMovements(product_id, movement_type, quantity_change, reason, reference_order_id)
        SELECT oi.product_id, 'IN', oi.quantity, CONCAT('Cancelled order #', NEW.order_id), NEW.order_id
        FROM OrderItems oi
        WHERE oi.order_id = NEW.order_id;
    END IF;
END//

DELIMITER ;

CREATE OR REPLACE VIEW vw_ProductInventory AS
SELECT
    p.product_id,
    p.name,
    p.unit_price,
    i.quantity,
    p.reorder_level,
    s.name AS supplier
FROM Products p
JOIN Inventory i ON p.product_id = i.product_id
JOIN Suppliers s ON p.supplier_id = s.supplier_id;

CREATE OR REPLACE VIEW vw_LowStock AS
SELECT product_id, name, unit_price, quantity, reorder_level, supplier
FROM vw_ProductInventory
WHERE quantity <= reorder_level;

CREATE OR REPLACE VIEW vw_OrderSummary AS
SELECT
    o.order_id,
    o.customer_name,
    o.order_date,
    o.status,
    COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total
FROM Orders o
LEFT JOIN OrderItems oi ON o.order_id = oi.order_id
GROUP BY o.order_id, o.customer_name, o.order_date, o.status;

CREATE OR REPLACE VIEW vw_InventoryValue AS
SELECT
    product_id,
    name,
    supplier,
    quantity,
    unit_price,
    quantity * unit_price AS stock_value
FROM vw_ProductInventory;

CREATE OR REPLACE VIEW vw_ProductSales AS
SELECT
    p.product_id,
    p.name,
    COALESCE(SUM(CASE WHEN o.status <> 'Cancelled' THEN oi.quantity ELSE 0 END), 0) AS total_units_sold,
    COALESCE(SUM(CASE WHEN o.status <> 'Cancelled' THEN oi.quantity * oi.unit_price ELSE 0 END), 0) AS sales_amount
FROM Products p
LEFT JOIN OrderItems oi ON p.product_id = oi.product_id
LEFT JOIN Orders o ON oi.order_id = o.order_id
GROUP BY p.product_id, p.name;

CREATE OR REPLACE VIEW vw_StockMovements AS
SELECT
    sm.movement_id,
    sm.movement_time,
    p.name AS product_name,
    sm.movement_type,
    sm.quantity_change,
    sm.reason,
    sm.reference_order_id
FROM StockMovements sm
JOIN Products p ON sm.product_id = p.product_id;

INSERT INTO Suppliers(name, contact_name, phone, email, city) VALUES
('TechSource India', 'Rahul Mehra', '9876500011', 'orders@techsource.example', 'Delhi'),
('OfficeMart Wholesale', 'Nisha Kapoor', '9876500022', 'sales@officemart.example', 'Mumbai'),
('PackPro Supplies', 'Amit Rao', '9876500033', 'support@packpro.example', 'Pune'),
('FurniCo Distributors', 'Sara Thomas', '9876500044', 'hello@furnico.example', 'Bengaluru');

INSERT INTO Products(name, unit_price, reorder_level, supplier_id) VALUES
('USB Keyboard', 850.00, 10, 1),
('Wireless Mouse', 650.00, 15, 1),
('24-inch Monitor', 11900.00, 5, 1),
('Barcode Scanner', 7800.00, 4, 2),
('Label Printer', 14500.00, 3, 2),
('Packing Tape Roll', 75.00, 50, 3),
('A4 Copy Paper Ream', 420.00, 40, 3),
('Ergonomic Desk Chair', 5200.00, 5, 4);

INSERT INTO Inventory(product_id, quantity) VALUES
(1, 25),
(2, 18),
(3, 7),
(4, 6),
(5, 4),
(6, 90),
(7, 55),
(8, 8);

INSERT INTO Orders(order_date, status, customer_name) VALUES
('2026-05-01', 'Completed', 'Anika Stores'),
('2026-05-06', 'Processing', 'Metro Office Hub'),
('2026-05-12', 'Processing', 'North Branch Admin');

INSERT INTO OrderItems(order_id, product_id, quantity, unit_price) VALUES
(1, 2, 5, 650.00),
(1, 6, 25, 75.00),
(2, 1, 3, 850.00),
(2, 4, 2, 7800.00),
(3, 3, 1, 11900.00),
(3, 5, 1, 14500.00),
(3, 7, 20, 420.00);
