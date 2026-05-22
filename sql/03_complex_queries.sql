USE project;

-- 1. Orders with totals and number of line items.
SELECT
    o.order_id,
    o.customer_name,
    o.order_date,
    o.status,
    COUNT(oi.item_id) AS line_count,
    SUM(oi.quantity * oi.unit_price) AS order_total
FROM Orders o
JOIN OrderItems oi ON o.order_id = oi.order_id
GROUP BY o.order_id, o.customer_name, o.order_date, o.status
ORDER BY order_total DESC;

-- 2. Low-stock report from the view.
SELECT product_id, name, quantity, reorder_level, supplier
FROM vw_LowStock
ORDER BY quantity ASC, name ASC;

-- 3. Inventory valuation by supplier.
SELECT
    supplier,
    COUNT(product_id) AS product_count,
    SUM(quantity) AS total_units,
    SUM(stock_value) AS total_stock_value
FROM vw_InventoryValue
GROUP BY supplier
ORDER BY total_stock_value DESC;

-- 4. Top selling products, excluding cancelled orders.
SELECT
    ps.product_id,
    ps.name,
    ps.total_units_sold,
    ps.sales_amount
FROM vw_ProductSales ps
WHERE ps.total_units_sold > 0
ORDER BY ps.sales_amount DESC;

-- 5. Products whose stock value is above the average product stock value.
SELECT product_id, name, quantity, unit_price, stock_value
FROM vw_InventoryValue
WHERE stock_value > (
    SELECT AVG(stock_value)
    FROM vw_InventoryValue
)
ORDER BY stock_value DESC;

-- 6. Customers who bought at least two distinct products.
SELECT
    o.customer_name,
    COUNT(DISTINCT oi.product_id) AS distinct_products,
    SUM(oi.quantity * oi.unit_price) AS total_spent
FROM Orders o
JOIN OrderItems oi ON o.order_id = oi.order_id
WHERE o.status <> 'Cancelled'
GROUP BY o.customer_name
HAVING COUNT(DISTINCT oi.product_id) >= 2
ORDER BY total_spent DESC;

-- 7. Transaction demo: create an order. The trigger validates stock and decrements inventory.
START TRANSACTION;

INSERT INTO Orders(order_date, status, customer_name)
VALUES (CURRENT_DATE(), 'Processing', 'Demo Customer');

SET @new_order_id = LAST_INSERT_ID();

INSERT INTO OrderItems(order_id, product_id, quantity, unit_price)
SELECT @new_order_id, product_id, 2, unit_price
FROM Products
WHERE name = 'USB Keyboard';

COMMIT;

-- 8. Trigger demo: cancelling an order restores stock through trg_orders_after_cancel.
UPDATE Orders
SET status = 'Cancelled'
WHERE order_id = @new_order_id;

SELECT *
FROM vw_StockMovements
WHERE reference_order_id = @new_order_id
ORDER BY movement_time;
