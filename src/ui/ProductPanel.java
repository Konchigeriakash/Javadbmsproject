package ui;

import dao.InventoryDAO;
import dao.ProductDAO;
import model.Product;
import ui.dialogs.AddProductDialog;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.List;

public class ProductPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;

    public ProductPanel() {
        setLayout(new BorderLayout(6, 6));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Name", "Unit Price (INR)", "Stock", "Reorder Level"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(24);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnAdd = new JButton("Add Product");
        JButton btnEdit = new JButton("Edit");
        JButton btnStock = new JButton("Update Stock");
        JButton btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");
        bar.add(btnAdd);
        bar.add(btnEdit);
        bar.add(btnStock);
        bar.add(btnDelete);
        bar.add(btnRefresh);
        add(bar, BorderLayout.SOUTH);

        btnAdd.addActionListener(e -> showAddDialog());
        btnEdit.addActionListener(e -> showEditDialog());
        btnStock.addActionListener(e -> showStockDialog());
        btnDelete.addActionListener(e -> deleteSelected());
        btnRefresh.addActionListener(e -> loadData());

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Product> products = productDAO.getAll();
        for (Product p : products) {
            tableModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getName(),
                    String.format("%.2f", p.getUnitPrice()),
                    p.getQuantity(),
                    p.getReorderLevel()
            });
        }
    }

    private void showAddDialog() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        AddProductDialog dialog = new AddProductDialog(owner, productDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadData();
        }
    }

    private void showEditDialog() {
        int row = selectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String price = (String) tableModel.getValueAt(row, 2);
        int reorder = (int) tableModel.getValueAt(row, 4);

        JTextField nameField = new JTextField(name, 16);
        JTextField priceField = new JTextField(price, 8);
        JTextField reorderField = new JTextField(String.valueOf(reorder), 8);

        JPanel form = new JPanel(new GridLayout(3, 2, 6, 6));
        form.add(new JLabel("Name:"));
        form.add(nameField);
        form.add(new JLabel("Unit price:"));
        form.add(priceField);
        form.add(new JLabel("Reorder level:"));
        form.add(reorderField);

        int result = JOptionPane.showConfirmDialog(
                this, form, "Edit Product", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double parsedPrice = Double.parseDouble(priceField.getText().trim());
                int parsedReorder = Integer.parseInt(reorderField.getText().trim());
                if (nameField.getText().trim().isEmpty() || parsedPrice < 0 || parsedReorder < 0) {
                    throw new NumberFormatException();
                }

                if (productDAO.update(id, nameField.getText(), parsedPrice, parsedReorder)) {
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Product was not updated.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid name, price, and reorder level.");
            }
        }
    }

    private void showStockDialog() {
        int row = selectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int stock = (int) tableModel.getValueAt(row, 3);
        JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(stock, 0, 1_000_000, 1));

        JPanel form = new JPanel(new GridLayout(2, 2, 6, 6));
        form.add(new JLabel("Product:"));
        form.add(new JLabel(name));
        form.add(new JLabel("New stock:"));
        form.add(stockSpinner);

        int result = JOptionPane.showConfirmDialog(
                this, form, "Update Stock", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int newStock = (Integer) stockSpinner.getValue();
            if (inventoryDAO.setStock(id, newStock)) {
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Stock was not updated.");
            }
        }
    }

    private void deleteSelected() {
        int row = selectedModelRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product first.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete product \"" + name + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (productDAO.delete(id)) {
                loadData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Product was not deleted. It may already be used in an order.",
                        "Delete failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int selectedModelRow() {
        int row = table.getSelectedRow();
        return row < 0 ? -1 : table.convertRowIndexToModel(row);
    }
}
