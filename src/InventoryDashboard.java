import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class InventoryDashboard extends JFrame {
    private static final Color BACKGROUND = new Color(245, 247, 250);
    private static final Color PRIMARY = new Color(37, 99, 235);

    private final JTable masterTable = new JTable();
    private final JTable productTable = new JTable();
    private final JTable inventoryTable = new JTable();
    private final JTable salesTable = new JTable();
    private final JTable purchaseTable = new JTable();
    private final JTable reportTable = new JTable();

    private final JComboBox<SelectItem> productCategoryCombo = new JComboBox<>();
    private final JComboBox<SelectItem> productSupplierCombo = new JComboBox<>();
    private final JComboBox<SelectItem> inventoryProductCombo = new JComboBox<>();
    private final JComboBox<SelectItem> salesCustomerCombo = new JComboBox<>();
    private final JComboBox<SelectItem> salesProductCombo = new JComboBox<>();
    private final JComboBox<SelectItem> purchaseSupplierCombo = new JComboBox<>();
    private final JComboBox<SelectItem> purchaseProductCombo = new JComboBox<>();

    public InventoryDashboard() throws SQLException {
        super("Inventory Project");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1050, 700);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);

        configureTable(masterTable);
        configureTable(productTable);
        configureTable(inventoryTable);
        configureTable(salesTable);
        configureTable(purchaseTable);
        configureTable(reportTable);

        setContentPane(createUi());
        refreshData();
    }

    private JPanel createUi() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(16, 18, 18, 18));

        JLabel title = new JLabel("Inventory Management System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        root.add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Master Data", masterPanel());
        tabs.addTab("Products", productsPanel());
        tabs.addTab("Inventory", inventoryPanel());
        tabs.addTab("Sales Orders", salesPanel());
        tabs.addTab("Purchase Orders", purchasePanel());
        tabs.addTab("Reports", reportsPanel());
        root.add(tabs, BorderLayout.CENTER);
        return root;
    }

    private JPanel masterPanel() {
        JPanel panel = basePanel();
        JTextField categoryName = new JTextField();
        JTextField supplierName = new JTextField();
        JTextField customerName = new JTextField();

        JPanel form = formPanel();
        form.add(new JLabel("Category Name"));
        form.add(categoryName);
        form.add(button("Add Category", event -> runSafely(() -> {
            execute("INSERT INTO categories(category_name) VALUES (?)",
                    InputValidator.requiredText(categoryName, "Category Name"));
            categoryName.setText("");
            refreshData();
        })));
        form.add(new JLabel("Supplier Name"));
        form.add(supplierName);
        form.add(button("Add Supplier", event -> runSafely(() -> {
            execute("INSERT INTO suppliers(supplier_name) VALUES (?)",
                    InputValidator.requiredText(supplierName, "Supplier Name"));
            supplierName.setText("");
            refreshData();
        })));
        form.add(new JLabel("Customer Name"));
        form.add(customerName);
        form.add(button("Add Customer", event -> runSafely(() -> {
            execute("INSERT INTO customers(customer_name) VALUES (?)",
                    InputValidator.requiredText(customerName, "Customer Name"));
            customerName.setText("");
            refreshData();
        })));
        form.add(new JLabel());
        form.add(new JLabel());
        form.add(button("Delete Selected", event -> runSafely(this::deleteSelectedMasterRow)));

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(masterTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel productsPanel() {
        JPanel panel = basePanel();
        JTextField productName = new JTextField();

        JPanel form = formPanel();
        form.add(new JLabel("Product Name"));
        form.add(productName);
        form.add(new JLabel());
        form.add(new JLabel("Category"));
        form.add(productCategoryCombo);
        form.add(new JLabel());
        form.add(new JLabel("Supplier"));
        form.add(productSupplierCombo);
        form.add(button("Add Product", event -> runSafely(() -> {
            execute("INSERT INTO products(product_name, category_id, supplier_id) VALUES (?, ?, ?)",
                    InputValidator.requiredText(productName, "Product Name"),
                    selectedId(productCategoryCombo),
                    selectedId(productSupplierCombo));
            productName.setText("");
            refreshData();
        })));
        form.add(new JLabel());
        form.add(new JLabel());
        form.add(button("Delete Selected", event -> runSafely(this::deleteSelectedProduct)));

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel inventoryPanel() {
        JPanel panel = basePanel();
        JTextField quantity = new JTextField("0");
        JTextField reorderLevel = new JTextField("0");

        JPanel form = formPanel();
        form.add(new JLabel("Product"));
        form.add(inventoryProductCombo);
        form.add(new JLabel());
        form.add(new JLabel("Quantity"));
        form.add(quantity);
        form.add(new JLabel());
        form.add(new JLabel("Reorder Level"));
        form.add(reorderLevel);
        form.add(button("Save Inventory", event -> runSafely(() -> {
            execute("""
                    INSERT INTO inventory(product_id, quantity, reorder_level)
                    VALUES (?, ?, ?)
                    ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), reorder_level = VALUES(reorder_level)
                    """,
                    selectedId(inventoryProductCombo),
                    InputValidator.nonNegativeInt(quantity, "Quantity"),
                    InputValidator.nonNegativeInt(reorderLevel, "Reorder Level"));
            refreshData();
        })));
        form.add(new JLabel());
        form.add(new JLabel());
        form.add(button("Delete Selected", event -> runSafely(() -> deleteSelectedById(
                inventoryTable, 0, "inventory row", "DELETE FROM inventory WHERE product_id = ?"))));

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel salesPanel() {
        JPanel panel = basePanel();
        JTextField quantity = new JTextField("1");

        JPanel form = formPanel();
        form.add(new JLabel("Customer"));
        form.add(salesCustomerCombo);
        form.add(new JLabel());
        form.add(new JLabel("Product"));
        form.add(salesProductCombo);
        form.add(new JLabel());
        form.add(new JLabel("Quantity"));
        form.add(quantity);
        form.add(button("Create Sales Order", event -> runSafely(() -> {
            createSalesOrder(selectedId(salesCustomerCombo), selectedId(salesProductCombo),
                    InputValidator.positiveInt(quantity, "Quantity"));
            quantity.setText("1");
            refreshData();
        })));
        form.add(new JLabel());
        form.add(new JLabel());
        form.add(button("Delete Selected", event -> runSafely(() -> deleteSelectedById(
                salesTable, 0, "sales order", "DELETE FROM sales_orders WHERE sales_order_id = ?"))));

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(salesTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel purchasePanel() {
        JPanel panel = basePanel();
        JTextField quantity = new JTextField("1");

        JPanel form = formPanel();
        form.add(new JLabel("Supplier"));
        form.add(purchaseSupplierCombo);
        form.add(new JLabel());
        form.add(new JLabel("Product"));
        form.add(purchaseProductCombo);
        form.add(new JLabel());
        form.add(new JLabel("Quantity"));
        form.add(quantity);
        form.add(button("Create Purchase Order", event -> runSafely(() -> {
            createPurchaseOrder(selectedId(purchaseSupplierCombo), selectedId(purchaseProductCombo),
                    InputValidator.positiveInt(quantity, "Quantity"));
            quantity.setText("1");
            refreshData();
        })));
        form.add(new JLabel());
        form.add(new JLabel());
        form.add(button("Delete Selected", event -> runSafely(() -> deleteSelectedById(
                purchaseTable, 0, "purchase order", "DELETE FROM purchase_orders WHERE purchase_order_id = ?"))));

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(purchaseTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel reportsPanel() {
        JPanel panel = basePanel();
        JButton refresh = button("Refresh", event -> runSafely(this::refreshData));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolbar.setOpaque(false);
        toolbar.add(refresh);
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportTable), BorderLayout.CENTER);
        return panel;
    }

    private void refreshData() throws SQLException {
        populateComboBox(productCategoryCombo,
                "SELECT category_id, category_name FROM categories ORDER BY category_name");
        populateComboBox(productSupplierCombo,
                "SELECT supplier_id, supplier_name FROM suppliers ORDER BY supplier_name");
        populateComboBox(inventoryProductCombo, "SELECT product_id, product_name FROM products ORDER BY product_name");
        populateComboBox(salesCustomerCombo, "SELECT customer_id, customer_name FROM customers ORDER BY customer_name");
        populateComboBox(salesProductCombo, "SELECT product_id, product_name FROM products ORDER BY product_name");
        populateComboBox(purchaseSupplierCombo,
                "SELECT supplier_id, supplier_name FROM suppliers ORDER BY supplier_name");
        populateComboBox(purchaseProductCombo, "SELECT product_id, product_name FROM products ORDER BY product_name");

        populateTable(masterTable, """
                SELECT 'CATEGORY' AS entity, category_id AS id, category_name AS name FROM categories
                UNION ALL
                SELECT 'SUPPLIER', supplier_id, supplier_name FROM suppliers
                UNION ALL
                SELECT 'CUSTOMER', customer_id, customer_name FROM customers
                ORDER BY entity, name
                """);
        populateTable(productTable, """
                SELECT p.product_id, p.product_name, c.category_name, s.supplier_name
                FROM products p
                JOIN categories c ON c.category_id = p.category_id
                JOIN suppliers s ON s.supplier_id = p.supplier_id
                ORDER BY p.product_id
                """);
        populateTable(inventoryTable, """
                SELECT p.product_id, p.product_name, i.quantity, i.reorder_level,
                       CASE WHEN i.quantity <= i.reorder_level THEN 'LOW STOCK' ELSE 'OK' END AS status
                FROM inventory i
                JOIN products p ON p.product_id = i.product_id
                ORDER BY p.product_name
                """);
        populateTable(salesTable, """
                SELECT so.sales_order_id, c.customer_name, p.product_name, soi.quantity
                FROM sales_orders so
                JOIN customers c ON c.customer_id = so.customer_id
                JOIN sales_order_items soi ON soi.sales_order_id = so.sales_order_id
                JOIN products p ON p.product_id = soi.product_id
                ORDER BY so.sales_order_id DESC
                """);
        populateTable(purchaseTable, """
                SELECT po.purchase_order_id, s.supplier_name, p.product_name, poi.quantity
                FROM purchase_orders po
                JOIN suppliers s ON s.supplier_id = po.supplier_id
                JOIN purchase_order_items poi ON poi.purchase_order_id = po.purchase_order_id
                JOIN products p ON p.product_id = poi.product_id
                ORDER BY po.purchase_order_id DESC
                """);
        populateTable(reportTable, """
                SELECT c.category_name, COUNT(p.product_id) AS products,
                       COALESCE(SUM(i.quantity), 0) AS total_quantity,
                       COALESCE(SUM(CASE WHEN i.quantity <= i.reorder_level THEN 1 ELSE 0 END), 0) AS low_stock_products
                FROM categories c
                LEFT JOIN products p ON p.category_id = c.category_id
                LEFT JOIN inventory i ON i.product_id = p.product_id
                GROUP BY c.category_id, c.category_name
                ORDER BY c.category_name
                """);
    }

    private static void createSalesOrder(int customerId, int productId, int quantity) throws SQLException {
        try (Connection connection = DatabaseManager.openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement orderStatement = connection.prepareStatement(
                    "INSERT INTO sales_orders(customer_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement itemStatement = connection.prepareStatement(
                            "INSERT INTO sales_order_items(sales_order_id, product_id, quantity) VALUES (?, ?, ?)")) {
                orderStatement.setInt(1, customerId);
                orderStatement.executeUpdate();
                ResultSet keys = orderStatement.getGeneratedKeys();
                keys.next();
                itemStatement.setInt(1, keys.getInt(1));
                itemStatement.setInt(2, productId);
                itemStatement.setInt(3, quantity);
                itemStatement.executeUpdate();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static void createPurchaseOrder(int supplierId, int productId, int quantity) throws SQLException {
        try (Connection connection = DatabaseManager.openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement orderStatement = connection.prepareStatement(
                    "INSERT INTO purchase_orders(supplier_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement itemStatement = connection.prepareStatement(
                            "INSERT INTO purchase_order_items(purchase_order_id, product_id, quantity) VALUES (?, ?, ?)")) {
                orderStatement.setInt(1, supplierId);
                orderStatement.executeUpdate();
                ResultSet keys = orderStatement.getGeneratedKeys();
                keys.next();
                itemStatement.setInt(1, keys.getInt(1));
                itemStatement.setInt(2, productId);
                itemStatement.setInt(3, quantity);
                itemStatement.executeUpdate();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static void execute(String sql, Object... values) throws SQLException {
        try (Connection connection = DatabaseManager.openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            statement.executeUpdate();
        }
    }

    private void deleteSelectedMasterRow() throws SQLException {
        int row = selectedModelRow(masterTable, "master data row");
        String entity = String.valueOf(masterTable.getModel().getValueAt(row, 0));
        Object id = masterTable.getModel().getValueAt(row, 1);
        String name = String.valueOf(masterTable.getModel().getValueAt(row, 2));

        if (confirmDelete(entity.toLowerCase() + " \"" + name + "\"")) {
            deleteMasterRow(entity, id);
            refreshData();
        }
    }

    private static void deleteMasterRow(String entity, Object id) throws SQLException {
        switch (entity) {
            case "CATEGORY" -> deleteCategory(id);
            case "SUPPLIER" -> deleteSupplier(id);
            case "CUSTOMER" -> deleteCustomerWithOrders(id);
            default -> throw new IllegalStateException("Unknown master data type: " + entity);
        }
    }

    private static void deleteCategory(Object categoryId) throws SQLException {
        int products = countReferences("SELECT COUNT(*) FROM products WHERE category_id = ?", categoryId);
        if (products > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete this category because " + products + " product(s) are assigned to it. "
                            + "Delete or move those products first.");
        }

        execute("DELETE FROM categories WHERE category_id = ?", categoryId);
    }

    private static void deleteSupplier(Object supplierId) throws SQLException {
        int products = countReferences("SELECT COUNT(*) FROM products WHERE supplier_id = ?", supplierId);
        if (products > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete this supplier because " + products + " product(s) use it. "
                            + "Delete or move those products first.");
        }

        int purchaseOrders = countReferences("SELECT COUNT(*) FROM purchase_orders WHERE supplier_id = ?", supplierId);
        if (purchaseOrders > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete this supplier because " + purchaseOrders + " purchase order(s) use it. "
                            + "Delete those purchase orders first.");
        }

        execute("DELETE FROM suppliers WHERE supplier_id = ?", supplierId);
    }

    private static void deleteCustomerWithOrders(Object customerId) throws SQLException {
        try (Connection connection = DatabaseManager.openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement orderStatement = connection.prepareStatement(
                    "DELETE FROM sales_orders WHERE customer_id = ?");
                    PreparedStatement customerStatement = connection.prepareStatement(
                            "DELETE FROM customers WHERE customer_id = ?")) {
                orderStatement.setObject(1, customerId);
                orderStatement.executeUpdate();
                customerStatement.setObject(1, customerId);
                customerStatement.executeUpdate();
                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private void deleteSelectedById(JTable table, int idColumn, String label, String sql) throws SQLException {
        int row = selectedModelRow(table, label);
        Object id = table.getModel().getValueAt(row, idColumn);
        String description = label + " #" + id;
        if (table.getModel().getColumnCount() > idColumn + 1) {
            description += " (" + table.getModel().getValueAt(row, idColumn + 1) + ")";
        }

        if (confirmDelete(description)) {
            execute(sql, id);
            refreshData();
        }
    }

    private void deleteSelectedProduct() throws SQLException {
        int row = selectedModelRow(productTable, "product");
        Object id = productTable.getModel().getValueAt(row, 0);
        String description = "product #" + id + " (" + productTable.getModel().getValueAt(row, 1) + ")";

        if (confirmDelete(description)) {
            deleteProduct(id);
            refreshData();
        }
    }

    private static void deleteProduct(Object productId) throws SQLException {
        int salesItems = countReferences("SELECT COUNT(*) FROM sales_order_items WHERE product_id = ?", productId);
        if (salesItems > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete this product because it appears in " + salesItems + " sales order(s). "
                            + "Delete those sales orders first.");
        }

        int purchaseItems = countReferences(
                "SELECT COUNT(*) FROM purchase_order_items WHERE product_id = ?", productId);
        if (purchaseItems > 0) {
            throw new IllegalArgumentException(
                    "Cannot delete this product because it appears in " + purchaseItems + " purchase order(s). "
                            + "Delete those purchase orders first.");
        }

        execute("DELETE FROM products WHERE product_id = ?", productId);
    }

    private static int countReferences(String sql, Object id) throws SQLException {
        try (Connection connection = DatabaseManager.openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private static int selectedModelRow(JTable table, String label) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            throw new IllegalArgumentException("Please select a " + label + " to delete.");
        }
        return table.convertRowIndexToModel(selectedRow);
    }

    private boolean confirmDelete(String description) {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return true;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete " + description + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        return choice == JOptionPane.YES_OPTION;
    }

    private void runSafely(SqlAction action) {
        try {
            action.run();
        } catch (Exception ex) {
            DialogHelper.showError(this, ex);
        }
    }

    private static int selectedId(JComboBox<SelectItem> comboBox) {
        SelectItem item = (SelectItem) comboBox.getSelectedItem();
        if (item == null) {
            throw new IllegalStateException("Please select a value.");
        }
        return item.getId();
    }

    private static void populateComboBox(JComboBox<SelectItem> comboBox, String sql) throws SQLException {
        SelectItem current = (SelectItem) comboBox.getSelectedItem();
        comboBox.removeAllItems();
        try (Connection connection = DatabaseManager.openConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                comboBox.addItem(new SelectItem(resultSet.getInt(1), resultSet.getString(2)));
            }
        }
        if (current != null) {
            for (int i = 0; i < comboBox.getItemCount(); i++) {
                if (comboBox.getItemAt(i).getId() == current.getId()) {
                    comboBox.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    private static void populateTable(JTable table, String sql) throws SQLException {
        try (Connection connection = DatabaseManager.openConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            table.setModel(buildModel(resultSet));
            table.setAutoCreateRowSorter(true);
            table.getColumnModel().getColumns().asIterator()
                    .forEachRemaining(column -> column.setPreferredWidth(150));
        }
    }

    private static DefaultTableModel buildModel(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        Vector<String> columns = new Vector<>();
        for (int column = 1; column <= metaData.getColumnCount(); column++) {
            columns.add(metaData.getColumnLabel(column));
        }

        Vector<Vector<Object>> rows = new Vector<>();
        while (resultSet.next()) {
            Vector<Object> row = new Vector<>();
            for (int column = 1; column <= metaData.getColumnCount(); column++) {
                row.add(resultSet.getObject(column));
            }
            rows.add(row);
        }

        return new DefaultTableModel(rows, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static JPanel basePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(12, 0, 0, 0));
        return panel;
    }

    private static JPanel formPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 3, 10, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));
        return panel;
    }

    private static JButton button(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.addActionListener(listener);
        return button;
    }

    private static void configureTable(JTable table) {
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setFont(table.getFont().deriveFont(13f));
    }

    @FunctionalInterface
    private interface SqlAction {
        void run() throws Exception;
    }
}
