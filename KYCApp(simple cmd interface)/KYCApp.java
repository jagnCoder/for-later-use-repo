import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
//_______________________________Connection Related Program__________________________________________________________
public class KYCApp {

    // --- JDBC Connection Parameters ---
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/kyc_db";
    private static final String USER = "root";
    private static final String PASS = "jaganMysql@123";

    // Scanner for user input
    private static final Scanner scanner = new Scanner(System.in);

    
    //✔~~~~~~~~~~~~~~~~~Establishes a connection to the MySQL database~~~~~~~~~~~~~~~~~
    public static Connection getConnection() {//A Connection object from java.sql
        Connection conn = null;
        try {
            Class.forName(JDBC_DRIVER);// Register the JDBC driver
            System.out.println("Connecting to the database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);// Open a connection
        } catch (SQLException se) {//prints SQL-related errors
            se.printStackTrace();
        } catch (ClassNotFoundException e){//error,if the JDBC driver class isn’t found in the classpath
            e.printStackTrace();
        }
        return conn;
    }
    //✔~~~~~~~~~~~~~~~~~Closes the database connection~~~~~~~~~~~~~~~~~
        public static void closeConnection(Connection conn){
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("\nDatabase connection closed.");
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

//__________________________________________Queries Related Program___________________________________________________
    //✔~~~~~~~~~~~~~~~~~Inserts a new user into the 'users' table~~~~~~~~~~~~~~~~~
    public static int createUser(Connection conn, String fullName, String dob, String address) {
        String sql = "INSERT INTO users (full_name, date_of_birth, address) VALUES (?, ?, ?)";
        int userId = -1;
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {//PreparedStatement-execute parameterized SQL queries safely, prepareStatement()-returns sql executabel prepaired statement

            pstmt.setString(1, fullName);
            pstmt.setString(2, dob);
            pstmt.setString(3, address);

            int affectedRows = pstmt.executeUpdate();//runs the SQL statements,returns the numberof affected rows

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                        System.out.println("User created successfully with ID: " + userId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userId;
    }

    //✔~~~~~~~~~~~~~~~~~Inserts a new document for a user into the 'documents' table~~~~~~~~~~~~~~~~~
    public static void uploadDocument(Connection conn, int userId, String docType, String docNumber) {
        String sql = "INSERT INTO documents (user_id, document_type, document_number) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, userId);
            pstmt.setString(2, docType);
            pstmt.setString(3, docNumber);

            int affectedRows = pstmt.executeUpdate();//execute sql statements & returns num of statements
            if (affectedRows > 0) {//fetch/uses the userID, if the insertion is not failed
                System.out.println("Document '" + docType + "' uploaded successfully for user " + userId + ".");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //✔~~~~~~~~~~~~~~~~~Reads and prints all user data and document from the database~~~~~~~~~~~~~~~~~
    public static void readUsers(Connection conn) {
        System.out.println("\n--- All Stored KYC Data ---");
        String usersSql = "SELECT id, full_name, date_of_birth, address FROM users";
        String docsSql = "SELECT document_type, document_number, verification_status FROM documents WHERE user_id = ?";//🔍 Why use ? in docsSql

        
        try (Statement stmt = conn.createStatement();//Statement-used to execute static SQL queries(no parameter), createStatement-creats and returns a statement Object
             ResultSet rsUsers = stmt.executeQuery(usersSql)) {//executeQuery execute SELECTSQL and retutns data fetched form the database, ResultSet holds rows data,used to move among rows& reading column values
            
            while (rsUsers.next()) {
                //Retrves current user's data
                int userId = rsUsers.getInt("id");//retrives int value from respective coulmn
                System.out.println("\nUser ID: " +userId);//retrives int value from respective coulmn
                System.out.println("Full Name: " + rsUsers.getString("full_name"));//retrives string value from respective coulmn
                System.out.println("Date of Birth: " + rsUsers.getString("date_of_birth"));
                System.out.println("Address: " + rsUsers.getString("address"));
                System.out.println("--- Documents ---");
                
                // Retrieve documents for the current user
                try (PreparedStatement pstmtDocs = conn.prepareStatement(docsSql)) {//↑
                    pstmtDocs.setInt(1, userId);//determines position(1) of stateholder(?) and their value(userId)
                    try (ResultSet rsDocs = pstmtDocs.executeQuery()) {//ResultSet stores the rows after executing respecting SQL
                        while (rsDocs.next()) {//.next to move through each row
                            System.out.println("  - Type: " + rsDocs.getString("document_type") + ", Number: " + rsDocs.getString("document_number") + ", Status: " +rsDocs.getString("verification_status"));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("\n--- End of Data ---");
    }

    //✔~~~~~~~~~~~~~~~~~ Updates the address of a user based on their ID~~~~~~~~~~~~~~~~~
    public static void updateUserAddress(Connection conn, int userId, String newAddress) {
        String sql = "UPDATE users SET address = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {//enhanced security against injection
            pstmt.setString(1, newAddress);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("User " + userId + "'s address updated successfully.");
            } else {
                System.out.println("No user found with ID " + userId + ".");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //✔~~~~~~~~~~~~~~~~~Deletes a user and all their associated documents~~~~~~~~~~~~~~~~~
    public static void deleteUser(Connection conn, int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {//reurns after precompiling the sql query
            pstmt.setInt(1, userId);//↑

            int affectedRows = pstmt.executeUpdate();//executes the query by sending it to database,returns the change information
            if (affectedRows > 0) {
                System.out.println("User " + userId + " and their documents deleted successfully.");
            } else {
                System.out.println("No user found with ID " + userId + ".");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//__________________________________________Main Menu Option___________________________________________________
    //✔~~~~~~~~~~~~~~~~~Displays the menu options to the user~~~~~~~~~~~~~~~~~

    public static void showMenu() {
        System.out.println("\n--- KYC Application Menu ---");
        System.out.println("1. Add a new user and documents");
        System.out.println("2. View all stored data");
        System.out.println("3. Update a user's address");
        System.out.println("4. Delete a user");
        System.out.println("5. Exit");
        System.out.print("Please enter your choice: ");
    }



//__________________________________________Main Programm___________________________________________________
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn == null) {
                System.err.println("Failed to establish a database connection. Exiting.");
                return;
            }

            boolean exit = true;
            while (exit) {
                showMenu();
                String choice = scanner.nextLine();
                switch (choice) {
                    case "1":
                        System.out.println("\n--- Add New User ---");
                        System.out.print("Enter full name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter date of birth (YYYY-MM-DD): ");
                        String dob = scanner.nextLine();
                        System.out.print("Enter address: ");
                        String address = scanner.nextLine();
                        
                        int newUserId = createUser(conn, name, dob, address);
                        
                        if (newUserId != -1) {
                            System.out.print("Enter document type (e.g., Passport): ");
                            String docType1 = scanner.nextLine();
                            System.out.print("Enter document number: ");
                            String docNumber1 = scanner.nextLine();
                            uploadDocument(conn, newUserId, docType1, docNumber1);
                            
                            System.out.print("Do you want to add another document? (yes/no): ");
                            if (scanner.nextLine().equalsIgnoreCase("yes")) {
                                System.out.print("Enter second document type: ");
                                String docType2 = scanner.nextLine();
                                System.out.print("Enter second document number: ");
                                String docNumber2 = scanner.nextLine();
                                uploadDocument(conn, newUserId, docType2, docNumber2);
                            }
                        }
                        break;
                    case "2":
                        readUsers(conn);
                        break;
                    case "3":
                        System.out.println("\n--- Update User Address ---");
                        System.out.print("Enter user ID to update: ");
                        try {
                            int updateUserId = Integer.parseInt(scanner.nextLine());
                            System.out.print("Enter new address: ");
                            String newAddress = scanner.nextLine();
                            updateUserAddress(conn, updateUserId, newAddress);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid user ID. Please enter a number.");
                        }
                        break;
                    case "4":
                        System.out.println("\n--- Delete User ---");
                        System.out.print("Enter user ID to delete: ");
                        try {
                            int deleteUserId = Integer.parseInt(scanner.nextLine());
                            deleteUser(conn, deleteUserId);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid user ID. Please enter a number.");
                        }
                        break;
                    case "5":
                        exit=false;
                        System.out.println("Exiting application.");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } finally {
            closeConnection(conn);
            scanner.close();        }
    }
}
