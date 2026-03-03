import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class KYCApp {

    // JDBC connection parameters
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/kyc_db";
    private static final String USER = "root";
    private static final String PASS = "jaganMysql@123";

    // Establish a connection to the MySQL database
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }

    // Close the database connection
    public static void closeConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    // Insert a new user and return generated ID
    public static int createUser(Connection conn, String fullName, String dob, String address) {
        String sql = "INSERT INTO users (full_name, date_of_birth, address) VALUES (?, ?, ?)";
        int userId = -1;
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, dob);
            pstmt.setString(3, address);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    // Insert a document record for a user
    public static void uploadDocument(Connection conn, int userId, String docType, String docNumber) {
        String sql = "INSERT INTO documents (user_id, document_type, document_number) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, docType);
            pstmt.setString(3, docNumber);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update a user's address
    public static void updateUserAddress(Connection conn, int userId, String newAddress) {
        String sql = "UPDATE users SET address = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newAddress);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete a user by ID
    public static void deleteUser(Connection conn, int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
