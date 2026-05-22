package dao;

import db.DBConnection;
import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDAO {

    public List<Product> getAll() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.product_id, p.name, p.unit_price, " +
                "COALESCE(i.quantity, 0) AS quantity, p.reorder_level " +
                "FROM Products p " +
                "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                "ORDER BY p.product_id";

        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                list.add(mapProduct(r));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Optional<Product> findById(int productId) {
        String sql = "SELECT p.product_id, p.name, p.unit_price, " +
                "COALESCE(i.quantity, 0) AS quantity, p.reorder_level " +
                "FROM Products p " +
                "LEFT JOIN Inventory i ON p.product_id = i.product_id " +
                "WHERE p.product_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet r = ps.executeQuery()) {
                if (r.next()) {
                    return Optional.of(mapProduct(r));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public int insert(String name, double price, int reorderLevel, int supplierId) {
        String sqlProduct = "INSERT INTO Products(name, unit_price, reorder_level, supplier_id) " +
                "VALUES (?, ?, ?, ?)";
        String sqlInventory = "INSERT INTO Inventory(product_id, quantity) VALUES (?, 0)";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int newId;
                try (PreparedStatement ps = c.prepareStatement(sqlProduct, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name.trim());
                    ps.setDouble(2, price);
                    ps.setInt(3, reorderLevel);
                    ps.setInt(4, supplierId);
                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            c.rollback();
                            return -1;
                        }
                        newId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = c.prepareStatement(sqlInventory)) {
                    ps.setInt(1, newId);
                    ps.executeUpdate();
                }

                c.commit();
                return newId;
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

    public boolean update(int productId, String name, double price, int reorderLevel) {
        String sql = "UPDATE Products SET name = ?, unit_price = ?, reorder_level = ? " +
                "WHERE product_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setDouble(2, price);
            ps.setInt(3, reorderLevel);
            ps.setInt(4, productId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int productId) {
        String sql = "DELETE FROM Products WHERE product_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Product mapProduct(ResultSet r) throws SQLException {
        return new Product(
                r.getInt("product_id"),
                r.getString("name"),
                r.getDouble("unit_price"),
                r.getInt("quantity"),
                r.getInt("reorder_level")
        );
    }
}
