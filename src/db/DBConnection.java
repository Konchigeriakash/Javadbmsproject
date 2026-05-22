package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/project?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "javauser";
    private static final String DEFAULT_PASS = "password123";

    private static final String URL = System.getProperty("inventory.db.url", DEFAULT_URL);
    private static final String USER = System.getProperty("inventory.db.user", DEFAULT_USER);
    private static final String PASS = System.getProperty("inventory.db.password", DEFAULT_PASS);

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found. Add mysql-connector-j to the classpath.", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void main(String[] args) throws Exception {
        try (Connection c = DBConnection.getConnection()) {
            System.out.println("Connected: " + !c.isClosed());
        }
    }
}
