package ui.dialogs;

import dao.InventoryDAO;
import dao.OrderDAO;
import dao.ProductDAO;
import model.Product;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlaceOrderDialog extends JDialog {

    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;
    private final InventoryDAO inventoryDAO;

    private boolean orderPlaced = false;

    private final JTextField customerField = new JTextField(18);
    private final JTextField productIdField = new JTextField(6);
    private final JTextField qtyField = new JTextField(6);

    private final DefaultTableModel cartModel = new DefaultTableModel(
            new String[]{"Product ID", "Product Name", "Qty", "Unit Price (INR)", "Line Total (INR)"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private final List<int[]> cartItems = new ArrayList<>();

    public PlaceOrderDialog(Frame parent,
                            OrderDAO orderDAO,
                            ProductDAO productDAO,
                            InventoryDAO inventoryDAO) {
        super(parent, "Place New Order", true);
        this.orderDAO = orderDAO;
        this.productDAO = productDAO;
        this.inventoryDAO = inventoryDAO;
        buildUI();
        setSize(620, 460);
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        setLayout(new BorderLayout(8, 8));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topPanel.add(new JLabel("Customer name:"));
        topPanel.add(customerField);
        add(topPanel, BorderLayout.NORTH);

        JTable cartTable = new JTable(cartModel);
        cartTable.setRowHeight(24);
        cartTable.getColumnModel().getColumn(0).setMaxWidth(90);
        cartTable.getColumnModel().getColumn(2).setMaxWidth(70);
        add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout(6, 6));
        south.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        JPanel entryRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        entryRow.setBorder(BorderFactory.createTitledBorder("Add item"));
        entryRow.add(new JLabel("Product ID:"));
        entryRow.add(productIdField);
        entryRow.add(new JLabel("Qty:"));
        entryRow.add(qtyField);

        JButton addItemBtn = new JButton("Add to cart");
        JButton removeBtn = new JButton("Remove selected");
        entryRow.add(addItemBtn);
        entryRow.add(removeBtn);
        south.add(entryRow, BorderLayout.NORTH);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton placeBtn = new JButton("Place Order");
        JButton cancelBtn = new JButton("Cancel");
        placeBtn.setBackground(new Color(60, 145, 85));
        placeBtn.setForeground(Color.WHITE);
        buttonRow.add(cancelBtn);
        buttonRow.add(placeBtn);
        south.add(buttonRow, BorderLayout.SOUTH);

        add(south, BorderLayout.SOUTH);

        addItemBtn.addActionListener(e -> addItemToCart());
        removeBtn.addActionListener(e -> removeSelectedItem(cartTable));
        placeBtn.addActionListener(e -> onPlaceOrder());
        cancelBtn.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(addItemBtn);
    }

    private void addItemToCart() {
        try {
            int productId = Integer.parseInt(productIdField.getText().trim());
            int qty = Integer.parseInt(qtyField.getText().trim());

            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
                return;
            }

            Optional<Product> product = productDAO.findById(productId);
            if (!product.isPresent()) {
                JOptionPane.showMessageDialog(this, "Product ID not found.");
                return;
            }

            int stock = inventoryDAO.getStock(productId);
            int alreadyRequested = requestedQuantity(productId);
            if (stock < alreadyRequested + qty) {
                JOptionPane.showMessageDialog(this,
                        "Insufficient stock. Available: " + stock +
                                ", already in cart: " + alreadyRequested);
                return;
            }

            Product found = product.get();
            double lineTotal = found.getUnitPrice() * qty;
            cartModel.addRow(new Object[]{
                    productId,
                    found.getName(),
                    qty,
                    String.format("%.2f", found.getUnitPrice()),
                    String.format("%.2f", lineTotal)
            });
            cartItems.add(new int[]{productId, qty});

            productIdField.setText("");
            qtyField.setText("");
            productIdField.requestFocus();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid integers for Product ID and Qty.");
        }
    }

    private int requestedQuantity(int productId) {
        int total = 0;
        for (int[] item : cartItems) {
            if (item[0] == productId) {
                total += item[1];
            }
        }
        return total;
    }

    private void removeSelectedItem(JTable cartTable) {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an item to remove.");
            return;
        }

        int modelRow = cartTable.convertRowIndexToModel(row);
        cartModel.removeRow(modelRow);
        cartItems.remove(modelRow);
    }

    private void onPlaceOrder() {
        String customer = customerField.getText().trim();
        if (customer.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a customer name.");
            return;
        }
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add at least one item before placing the order.");
            return;
        }

        int newId = orderDAO.placeOrder(customer, cartItems);
        if (newId > 0) {
            JOptionPane.showMessageDialog(this, "Order #" + newId + " placed successfully.");
            orderPlaced = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Order failed. Check stock levels and database connection, then try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isOrderPlaced() {
        return orderPlaced;
    }
}
