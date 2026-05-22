package ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Inventory Management System");
        setSize(960, 640);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);   // centre on screen

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Products",   new ProductPanel());
        tabs.addTab("Orders",     new OrderPanel());
        tabs.addTab("Reports",    new ReportPanel());

        add(tabs, BorderLayout.CENTER);

        JLabel status = new JLabel("  Ready");
        status.setBorder(BorderFactory.createEtchedBorder());
        add(status, BorderLayout.SOUTH);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Use default Swing look and feel when the system one is unavailable.
            }
            new MainFrame();
        });
    }
}
