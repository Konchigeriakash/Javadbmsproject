package ui;

import db.DBConnection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ReportPanel extends JPanel {

    public ReportPanel() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Low Stock", new ReportTablePanel(
                "Products at or below reorder level",
                new String[]{"Product ID", "Product", "Current Stock", "Reorder Level", "Supplier"},
                "SELECT product_id, name, quantity, reorder_level, supplier FROM vw_LowStock ORDER BY quantity ASC",
                2));
        tabs.addTab("Inventory Value", new ReportTablePanel(
                "Current stock valuation",
                new String[]{"Product ID", "Product", "Supplier", "Qty", "Unit Price", "Stock Value"},
                "SELECT product_id, name, supplier, quantity, unit_price, stock_value FROM vw_InventoryValue ORDER BY stock_value DESC",
                -1));
        tabs.addTab("Product Sales", new ReportTablePanel(
                "Sales grouped by product",
                new String[]{"Product ID", "Product", "Units Sold", "Sales Amount"},
                "SELECT product_id, name, total_units_sold, sales_amount FROM vw_ProductSales ORDER BY sales_amount DESC",
                -1));
        tabs.addTab("Stock Movements", new ReportTablePanel(
                "Recent inventory changes",
                new String[]{"Time", "Product", "Type", "Change", "Reason"},
                "SELECT movement_time, product_name, movement_type, quantity_change, reason FROM vw_StockMovements ORDER BY movement_time DESC",
                -1));

        add(tabs, BorderLayout.CENTER);
    }

    private static class ReportTablePanel extends JPanel {
        private final DefaultTableModel model;
        private final String sql;

        ReportTablePanel(String title, String[] columns, String sql, int alertColumn) {
            this.sql = sql;
            setLayout(new BorderLayout(6, 6));

            JLabel label = new JLabel(title);
            label.setBorder(BorderFactory.createEmptyBorder(0, 2, 4, 2));
            add(label, BorderLayout.NORTH);

            model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };

            JTable table = new JTable(model);
            table.setRowHeight(24);
            if (alertColumn >= 0) {
                table.setDefaultRenderer(Object.class, new LowStockRenderer(alertColumn));
            }
            add(new JScrollPane(table), BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            JButton refresh = new JButton("Refresh");
            refresh.addActionListener(e -> loadData());
            bottom.add(refresh);
            add(bottom, BorderLayout.SOUTH);

            loadData();
        }

        private void loadData() {
            model.setRowCount(0);
            try (Connection c = DBConnection.getConnection();
                 Statement s = c.createStatement();
                 ResultSet r = s.executeQuery(sql)) {
                int columnCount = model.getColumnCount();
                while (r.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 0; i < columnCount; i++) {
                        row[i] = r.getObject(i + 1);
                    }
                    model.addRow(row);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static class LowStockRenderer extends DefaultTableCellRenderer {
        private final int stockColumn;

        LowStockRenderer(int stockColumn) {
            this.stockColumn = stockColumn;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                Object stockValue = table.getModel().getValueAt(row, stockColumn);
                int stock = stockValue instanceof Number ? ((Number) stockValue).intValue() : 0;
                c.setBackground(stock == 0 ? new Color(255, 205, 205) : new Color(255, 245, 210));
            }
            return c;
        }
    }
}
