package dao;

import db.DBConnection;
import model.Order;
import model.OrderItem;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public List<Order> getAll() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.customer_name, o.order_date, o.status, " +
                "COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total " +
                "FROM Orders o " +
                "LEFT JOIN OrderItems oi ON o.order_id = oi.order_id " +
                "GROUP BY o.order_id, o.customer_name, o.order_date, o.status " +
                "ORDER BY o.order_id DESC";

        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                list.add(new Order(
                        r.getInt("order_id"),
                        r.getString("customer_name"),
                        r.getString("order_date"),
                        r.getString("status"),
                        r.getDouble("total")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<OrderItem> getItemsByOrder(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.item_id, oi.order_id, oi.product_id, " +
                "p.name AS product_name, oi.quantity, oi.unit_price " +
                "FROM OrderItems oi " +
                "JOIN Products p ON oi.product_id = p.product_id " +
                "WHERE oi.order_id = ? " +
                "ORDER BY oi.item_id";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet r = ps.executeQuery()) {
                while (r.next()) {
                    items.add(new OrderItem(
                            r.getInt("item_id"),
                            r.getInt("order_id"),
                            r.getInt("product_id"),
                            r.getString("product_name"),
                            r.getInt("quantity"),
                            r.getDouble("unit_price")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public int placeOrder(String customerName, int productId, int quantity) {
        List<int[]> items = new ArrayList<>();
        items.add(new int[]{productId, quantity});
        return placeOrder(customerName, items);
    }

    public int placeOrder(String customerName, List<int[]> items) {
        if (customerName == null || customerName.trim().isEmpty() || items == null || items.isEmpty()) {
            return -1;
        }

        String sqlOrder = "INSERT INTO Orders(order_date, status, customer_name) VALUES (?, 'Processing', ?)";
        String sqlItem = "INSERT INTO OrderItems(order_id, product_id, quantity, unit_price) " +
                "VALUES (?, ?, ?, (SELECT unit_price FROM Products WHERE product_id = ?))";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int newOrderId;
                try (PreparedStatement ps = c.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setDate(1, new Date(System.currentTimeMillis()));
                    ps.setString(2, customerName.trim());
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            c.rollback();
                            return -1;
                        }
                        newOrderId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(sqlItem)) {
                    for (int[] pair : items) {
                        if (pair.length < 2 || pair[1] <= 0) {
                            c.rollback();
                            return -1;
                        }
                        ps.setInt(1, newOrderId);
                        ps.setInt(2, pair[0]);
                        ps.setInt(3, pair[1]);
                        ps.setInt(4, pair[0]);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                c.commit();
                return newOrderId;
            } catch (SQLException e) {
                c.rollback();
                e.printStackTrace();
                return -1;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean cancelOrder(int orderId) {
        String sql = "UPDATE Orders SET status = 'Cancelled' " +
                "WHERE order_id = ? AND status <> 'Cancelled'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
