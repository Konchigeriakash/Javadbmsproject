package ui;

import dao.InventoryDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import model.OrderItem;
import ui.dialogs.PlaceOrderDialog;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.List;

public class OrderPanel extends JPanel {

    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final DefaultTableModel model;
    private final JTable table;

    public OrderPanel() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"Order ID", "Customer", "Date", "Status", "Total (INR)"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnNew = new JButton("Place Order");
        JButton btnItems = new JButton("View Items");
        JButton btnCancel = new JButton("Cancel Order");
        JButton btnRefresh = new JButton("Refresh");
        bar.add(btnNew);
        bar.add(btnItems);
        bar.add(btnCancel);
        bar.add(btnRefresh);
        add(bar, BorderLayout.SOUTH);

        btnNew.addActionListener(e -> showPlaceOrderDialog());
        btnItems.addActionListener(e -> showOrderItems());
        btnCancel.addActionListener(e -> cancelSelectedOrder());
        btnRefresh.addActionListener(e -> loadData());

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        orderDAO.getAll().forEach(o -> model.addRow(new Object[]{
                o.getOrderId(),
                o.getCustomerName(),
                o.getOrderDate(),
                o.getStatus(),
                String.format("%.2f", o.getTotal())
        }));
    }

    private void showPlaceOrderDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        PlaceOrderDialog dialog = new PlaceOrderDialog(owner, orderDAO, productDAO, inventoryDAO);
        dialog.setVisible(true);
        if (dialog.isOrderPlaced()) {
            loadData();
        }
    }

    private void showOrderItems() {
        int row = selectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order first.");
            return;
        }

        int orderId = (int) model.getValueAt(row, 0);
        List<OrderItem> items = orderDAO.getItemsByOrder(orderId);

        DefaultTableModel itemModel = new DefaultTableModel(
                new String[]{"Product ID", "Product", "Qty", "Unit Price (INR)", "Line Total (INR)"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (OrderItem item : items) {
            itemModel.addRow(new Object[]{
                    item.getProductId(),
                    item.getProductName(),
                    item.getQuantity(),
                    String.format("%.2f", item.getUnitPrice()),
                    String.format("%.2f", item.getLineTotal())
            });
        }

        JTable itemTable = new JTable(itemModel);
        itemTable.setRowHeight(24);
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Order #" + orderId + " Items", true);
        dialog.add(new JScrollPane(itemTable), BorderLayout.CENTER);
        dialog.setSize(620, 320);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void cancelSelectedOrder() {
        int row = selectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order first.");
            return;
        }

        int orderId = (int) model.getValueAt(row, 0);
        String status = String.valueOf(model.getValueAt(row, 3));
        if ("Cancelled".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This order is already cancelled.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Cancel order #" + orderId + "? Stock will be restored.",
                "Confirm Cancel",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (orderDAO.cancelOrder(orderId)) {
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Order was not cancelled.");
            }
        }
    }

    private int selectedModelRow() {
        int row = table.getSelectedRow();
        return row < 0 ? -1 : table.convertRowIndexToModel(row);
    }
}
