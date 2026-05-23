import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    public static final String DB_NAME = "javadb";
    private static final String URL =
            "jdbc:mysql://localhost:3306/javadb";
    private static final String USER = "root";
    private static final String PASS = "Akt@2005";

    private DatabaseManager() {
    }

    public static Connection openConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
