package ui.dialogs;

import dao.ProductDAO;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class AddProductDialog extends JDialog {

    private final ProductDAO dao;
    private boolean saved = false;

    private final JTextField nameField = new JTextField(18);
    private final JTextField priceField = new JTextField(10);
    private final JTextField reorderField = new JTextField(10);
    private final JTextField supplierField = new JTextField(10);

    public AddProductDialog(Frame parent, ProductDAO dao) {
        super(parent, "Add New Product", true);
        this.dao = dao;
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    private void buildUI() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 6, 14));

        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.anchor = GridBagConstraints.EAST;
        labelConstraints.insets = new Insets(4, 4, 4, 8);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.weightx = 1.0;
        fieldConstraints.insets = new Insets(4, 0, 4, 4);

        String[] labels = {"Product name:", "Unit price (INR):", "Reorder level:", "Supplier ID:"};
        JTextField[] fields = {nameField, priceField, reorderField, supplierField};

        for (int i = 0; i < labels.length; i++) {
            labelConstraints.gridx = 0;
            labelConstraints.gridy = i;
            fieldConstraints.gridx = 1;
            fieldConstraints.gridy = i;
            form.add(new JLabel(labels[i]), labelConstraints);
            form.add(fields[i], fieldConstraints);
        }

        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        saveBtn.addActionListener(e -> onSave());
        cancelBtn.addActionListener(e -> dispose());

        getRootPane().setDefaultButton(saveBtn);
        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product name is required.");
            return;
        }

        try {
            double price = Double.parseDouble(priceField.getText().trim());
            int reorder = Integer.parseInt(reorderField.getText().trim());
            int supplierId = Integer.parseInt(supplierField.getText().trim());

            if (price < 0 || reorder < 0 || supplierId <= 0) {
                throw new NumberFormatException();
            }

            int newId = dao.insert(name, price, reorder, supplierId);
            if (newId > 0) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Product was not saved. Check that the supplier ID exists.",
                        "Save failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Enter valid non-negative numbers for price and reorder level. Supplier ID must be positive.");
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
