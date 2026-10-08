import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    private static final String URL = "jdbc:mysql://localhost:3306/jdbc_db";
    private static final String USERNAME = "robel";
    private static final String PASSWORD = "@robel1234";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            System.out.println("====================================");
            System.out.println("       ROBEL'S JDBC USER MANAGER    ");
            System.out.println("====================================");
            System.out.println("Database connection established.");

            // 1. Create table
            createTable(connection);

            // 2. CREATE (Insert)
            printSection("Adding users");
            int userId1 = createUser(connection, "Robel Alemayehu", "robel@example.com");
            int userId2 = createUser(connection, "Sara Bekele", "sara@example.com");

            // 3. READ (Select)
            printSection("Current user directory");
            readUsers(connection);

            // 4. UPDATE
            printSection("Updating user " + userId1);
            updateUser(connection, userId1, "Robel Alemayehu", "robel.alemayehu@example.com");

            printSection("Directory after update");
            readUsers(connection);

            // 5. DELETE
            printSection("Removing user " + userId2);
            deleteUser(connection, userId2);

            printSection("Final user directory");
            readUsers(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void printSection(String title) {
        System.out.println("\n--- " + title + " ---");
    }

    private static void createTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "name VARCHAR(100) NOT NULL, " +
                     "email VARCHAR(100) NOT NULL" +
                     ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'users' verified/created successfully.");
        }
    }

    private static int createUser(Connection conn, String name, String email) throws SQLException {
        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        System.out.println("Created user: " + name + " (ID: " + generatedId + ")");
                        return generatedId;
                    }
                }
            }
        }
        return -1;
    }

    private static void readUsers(Connection conn) throws SQLException {
        String sql = "SELECT id, name, email FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                System.out.printf("#%-4d  %-22s  %s%n", id, name, email);
            }
        }
    }

    private static void updateUser(Connection conn, int id, String newName, String newEmail) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newName);
            pstmt.setString(2, newEmail);
            pstmt.setInt(3, id);
            int rowsUpdated = pstmt.executeUpdate();
            System.out.println("Updated " + rowsUpdated + " row(s) for User ID: " + id);
        }
    }

    private static void deleteUser(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsDeleted = pstmt.executeUpdate();
            System.out.println("Deleted " + rowsDeleted + " row(s) for User ID: " + id);
        }
    }
}