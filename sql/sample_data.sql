USE javadb;

INSERT INTO categories(category_name) VALUES
('Electronics'),
('Stationery'),
('Groceries');

INSERT INTO suppliers(supplier_name) VALUES
('TechSource Distributors'),
('OfficePro Wholesale'),
('DailyMart Supply');

INSERT INTO products(product_name, category_id, supplier_id) VALUES
('Wireless Keyboard', 1, 1),
('Optical Mouse', 1, 1),
('A4 Notebook', 2, 2),
('Premium Tea 500g', 3, 3);

INSERT INTO inventory(product_id, quantity, reorder_level) VALUES
(1, 30, 10),
(2, 40, 15),
(3, 100, 25),
(4, 18, 20);

INSERT INTO customers(customer_name) VALUES
('Aarav Stores'),
('Neha Retail');

INSERT INTO sales_orders(customer_id) VALUES
(1);

INSERT INTO sales_order_items(sales_order_id, product_id, quantity) VALUES
(1, 2, 3);

INSERT INTO purchase_orders(supplier_id) VALUES
(1);

INSERT INTO purchase_order_items(purchase_order_id, product_id, quantity) VALUES
(1, 1, 5);
