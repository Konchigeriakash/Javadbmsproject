USE javadb;

-- Categories and the products under them.
SELECT c.category_name, p.product_name
FROM categories c
JOIN products p ON p.category_id = c.category_id
ORDER BY c.category_name, p.product_name;

-- Products supplied by each supplier.
SELECT s.supplier_name, p.product_name
FROM suppliers s
JOIN products p ON p.supplier_id = s.supplier_id
ORDER BY s.supplier_name, p.product_name;

-- Inventory status from the PRODUCT to INVENTORY relationship.
SELECT p.product_name, i.quantity, i.reorder_level,
       CASE WHEN i.quantity <= i.reorder_level THEN 'LOW STOCK' ELSE 'OK' END AS status
FROM products p
JOIN inventory i ON i.product_id = p.product_id
ORDER BY p.product_name;

-- Sales orders placed by customers.
SELECT so.sales_order_id, c.customer_name, p.product_name, soi.quantity
FROM sales_orders so
JOIN customers c ON c.customer_id = so.customer_id
JOIN sales_order_items soi ON soi.sales_order_id = so.sales_order_id
JOIN products p ON p.product_id = soi.product_id
ORDER BY so.sales_order_id;

-- Purchase orders placed with suppliers.
SELECT po.purchase_order_id, s.supplier_name, p.product_name, poi.quantity
FROM purchase_orders po
JOIN suppliers s ON s.supplier_id = po.supplier_id
JOIN purchase_order_items poi ON poi.purchase_order_id = po.purchase_order_id
JOIN products p ON p.product_id = poi.product_id
ORDER BY po.purchase_order_id;

-- Summary by category.
SELECT c.category_name, COUNT(p.product_id) AS products,
       COALESCE(SUM(i.quantity), 0) AS total_quantity
FROM categories c
LEFT JOIN products p ON p.category_id = c.category_id
LEFT JOIN inventory i ON i.product_id = p.product_id
GROUP BY c.category_id, c.category_name
ORDER BY c.category_name;
