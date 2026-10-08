import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    private static final String SERVER_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "StudentsDB";
    private static final String USERNAME = "Robel";
    private static final String PASSWORD = "@Robel1234";

    public static void main(String[] args) {
        try (Connection server = DriverManager.getConnection(SERVER_URL, USERNAME, PASSWORD);
             Statement stmt = server.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("Database '" + DB_NAME + "' ready.");
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try (Connection connection = DriverManager.getConnection(SERVER_URL + DB_NAME, USERNAME, PASSWORD)) {
            System.out.println("Database connection established.");

            // create table
            createTable(connection);

            printSection("Inserting students");
            int firstId = createStudent(connection, "John", "Doe", 90);
            String[][] more = {
                {"Aster", "Nega", "85"},    {"Jemal", "Edris", "72"},
                {"Haile", "Anaol", "91"},   {"Teddy", "Habtu", "64"},
                {"Selam", "Tesfaye", "78"}, {"Dawit", "Kebede", "88"},
                {"Hana", "Mulugeta", "95"}, {"Abel", "Girma", "69"},
                {"Meron", "Alemu", "82"},   {"Yonas", "Bekele", "76"}
            };
            int lastId = firstId;
            for (String[] s : more) {
                lastId = createStudent(connection, s[0], s[1], Integer.parseInt(s[2]));
            }

            // retrieve five rows
            printSection("First five students");
            readStudents(connection);

            // update firstname by id
            printSection("Updating student " + firstId);
            updateFirstName(connection, firstId, "Jonathan");
            readStudents(connection);

            // delete by id
            printSection("Deleting student " + lastId);
            deleteStudent(connection, lastId);
            readStudents(connection);

            // average grade
            printSection("Average grade");
            calculateAverageGrade(connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void printSection(String title) {
        System.out.println("\n--- " + title + " ---");
    }

    private static void createTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS students (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "firstname VARCHAR(255), " +
                     "lastname VARCHAR(255), " +
                     "grade INT)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            stmt.executeUpdate("TRUNCATE TABLE students");
            System.out.println("Table 'students' ready.");
        }
    }

    private static int createStudent(Connection conn, String first, String last, int grade) throws SQLException {
        String sql = "INSERT INTO students (firstname, lastname, grade) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, first);
            pstmt.setString(2, last);
            pstmt.setInt(3, grade);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    System.out.println("Created student: " + first + " " + last + " (ID: " + id + ")");
                    return id;
                }
            }
        }
        return -1;
    }

    private static void readStudents(Connection conn) throws SQLException {
        String sql = "SELECT id, firstname, lastname, grade FROM students LIMIT 5";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf("#%-4d  %-10s %-10s  Grade: %d%n",
                        rs.getInt("id"), rs.getString("firstname"),
                        rs.getString("lastname"), rs.getInt("grade"));
            }
        }
    }

    private static void updateFirstName(Connection conn, int id, String newFirstName) throws SQLException {
        String sql = "UPDATE students SET firstname = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newFirstName);
            pstmt.setInt(2, id);
            System.out.println("Updated " + pstmt.executeUpdate() + " row(s) for ID: " + id);
        }
    }

    private static void deleteStudent(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            System.out.println("Deleted " + pstmt.executeUpdate() + " row(s) for ID: " + id);
        }
    }

    private static void calculateAverageGrade(Connection conn) throws SQLException {
        String sql = "SELECT AVG(grade) AS average_grade FROM students";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                System.out.printf("Average Grade: %.2f%n", rs.getDouble("average_grade"));
            }
        }
    }
}