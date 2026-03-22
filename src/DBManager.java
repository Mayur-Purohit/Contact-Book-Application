import java.sql.*;
import java.util.*;

public class DBManager {
    private Connection con;
    private String dburl = "jdbc:mysql://localhost:3306/ContactBookManagement2";
    private String dbuser = "root";
    private String dbpass = "";

    public DBManager() throws SQLException {
        this.con = DriverManager.getConnection(dburl, dbuser, dbpass);
        setupDatabase();
    }

    private void setupDatabase() throws SQLException {
        // Create backup logs table
        String backup = "CREATE TABLE IF NOT EXISTS BackupLogs (" +
                "log_id INT AUTO_INCREMENT PRIMARY KEY," +
                "operation_type ENUM('INSERT','DELETE') NOT NULL," +
                "book_name VARCHAR(100)," +
                "name VARCHAR(50)," +
                "phone VARCHAR(15)," +
                "email VARCHAR(100)," +
                "address VARCHAR(255)," +
                "age INT," +
                "company VARCHAR(100)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        Statement backupStmt = con.createStatement();
        backupStmt.executeUpdate(backup);

        // Create stored procedures
        createStoredProcedures();
    }

    private void createStoredProcedures() throws SQLException {
        Statement stmt = con.createStatement();

        // Drop existing procedures if they exist
        try {
            stmt.execute("DROP PROCEDURE IF EXISTS sp_insert_contact");
            stmt.execute("DROP PROCEDURE IF EXISTS sp_update_contact");
            stmt.execute("DROP PROCEDURE IF EXISTS sp_delete_contact");
            stmt.execute("DROP PROCEDURE IF EXISTS sp_get_contacts");
            stmt.execute("DROP PROCEDURE IF EXISTS sp_create_contact_book");
            stmt.execute("DROP PROCEDURE IF EXISTS sp_drop_contact_book");
        } catch (SQLException e) {
            // Ignore if procedures don't exist
        }

        // Create insert contact procedure
        String insertProc = "CREATE PROCEDURE sp_insert_contact(" +
                "IN p_table_name VARCHAR(100), " +
                "IN p_name VARCHAR(50), " +
                "IN p_phone VARCHAR(15), " +
                "IN p_email VARCHAR(100), " +
                "IN p_address VARCHAR(255), " +
                "IN p_age INT, " +
                "IN p_company VARCHAR(100)) " +
                "BEGIN " +
                "SET @sql = CONCAT('INSERT INTO ', p_table_name, ' (name, phone, email, address, age, company) VALUES (?, ?, ?, ?, ?, ?)'); " +
                "PREPARE stmt FROM @sql; " +
                "EXECUTE stmt USING p_name, p_phone, p_email, p_address, p_age, p_company; " +
                "DEALLOCATE PREPARE stmt; " +
                "END";

        // Create update contact procedure
        String updateProc = "CREATE PROCEDURE sp_update_contact(" +
                "IN p_table_name VARCHAR(100), " +
                "IN p_id INT, " +
                "IN p_name VARCHAR(50), " +
                "IN p_phone VARCHAR(15), " +
                "IN p_email VARCHAR(100), " +
                "IN p_address VARCHAR(255), " +
                "IN p_age INT, " +
                "IN p_company VARCHAR(100)) " +
                "BEGIN " +
                "SET @sql = CONCAT('UPDATE ', p_table_name, ' SET name=?, phone=?, email=?, address=?, age=?, company=? WHERE id=?'); " +
                "PREPARE stmt FROM @sql; " +
                "EXECUTE stmt USING p_name, p_phone, p_email, p_address, p_age, p_company, p_id; " +
                "DEALLOCATE PREPARE stmt; " +
                "END";

        // Create delete contact procedure
        String deleteProc = "CREATE PROCEDURE sp_delete_contact(" +
                "IN p_table_name VARCHAR(100), " +
                "IN p_name VARCHAR(50), " +
                "IN p_phone VARCHAR(15)) " +
                "BEGIN " +
                "SET @sql = CONCAT('DELETE FROM ', p_table_name, ' WHERE name = ? AND phone = ? LIMIT 1'); " +
                "PREPARE stmt FROM @sql; " +
                "EXECUTE stmt USING p_name, p_phone; " +
                "DEALLOCATE PREPARE stmt; " +
                "END";

        // Create get contacts procedure
        String getContactsProc = "CREATE PROCEDURE sp_get_contacts(" +
                "IN p_table_name VARCHAR(100)) " +
                "BEGIN " +
                "SET @sql = CONCAT('SELECT * FROM ', p_table_name); " +
                "PREPARE stmt FROM @sql; " +
                "EXECUTE stmt; " +
                "DEALLOCATE PREPARE stmt; " +
                "END";

        // Create contact book procedure
        String createBookProc = "CREATE PROCEDURE sp_create_contact_book(" +
                "IN p_book_name VARCHAR(100)) " +
                "BEGIN " +
                "SET @sql = CONCAT('CREATE TABLE IF NOT EXISTS ', p_book_name, ' (', " +
                "'id INT AUTO_INCREMENT PRIMARY KEY,', " +
                "'name VARCHAR(50),', " +
                "'phone VARCHAR(15),', " +
                "'email VARCHAR(100),', " +
                "'address VARCHAR(255),', " +
                "'age INT,', " +
                "'company VARCHAR(100))'); " +
                "PREPARE stmt FROM @sql; " +
                "EXECUTE stmt; " +
                "DEALLOCATE PREPARE stmt; " +
                "END";

        // Create drop book procedure
        String dropBookProc = "CREATE PROCEDURE sp_drop_contact_book(" +
                "IN p_book_name VARCHAR(100)) " +
                "BEGIN " +
                "SET @sql = CONCAT('DROP TABLE IF EXISTS ', p_book_name); " +
                "PREPARE stmt FROM @sql; " +
                "EXECUTE stmt; " +
                "DEALLOCATE PREPARE stmt; " +
                "END";

        stmt.execute(insertProc);
        stmt.execute(updateProc);
        stmt.execute(deleteProc);
        stmt.execute(getContactsProc);
        stmt.execute(createBookProc);
        stmt.execute(dropBookProc);
    }

    public void createContactBook(String bookName) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{ call sp_create_contact_book(?) }")) {
            cs.setString(1, bookName);
            cs.executeUpdate();
        }
        createTriggers(bookName);
    }

    private void createTriggers(String bookName) throws SQLException {
        Statement stmt = con.createStatement();

        String insertTriggerName = "trg_after_insert_" + bookName;
        stmt.executeUpdate("DROP TRIGGER IF EXISTS " + insertTriggerName);
        String insertTriggerSQL = "CREATE TRIGGER " + insertTriggerName + " " +
                "AFTER INSERT ON " + bookName + " " +
                "FOR EACH ROW " +
                "INSERT INTO BackupLogs (operation_type, book_name, name, phone, email, address, age, company) " +
                "VALUES ('INSERT', '" + bookName + "', NEW.name, NEW.phone, NEW.email, NEW.address, NEW.age, NEW.company)";
        stmt.executeUpdate(insertTriggerSQL);

        String deleteTriggerName = "trg_after_delete_" + bookName;
        stmt.executeUpdate("DROP TRIGGER IF EXISTS " + deleteTriggerName);
        String deleteTriggerSQL = "CREATE TRIGGER " + deleteTriggerName + " " +
                "AFTER DELETE ON " + bookName + " " +
                "FOR EACH ROW " +
                "INSERT INTO BackupLogs (operation_type, book_name, name, phone, email, address, age, company) " +
                "VALUES ('DELETE', '" + bookName + "', OLD.name, OLD.phone, OLD.email, OLD.address, OLD.age, OLD.company)";
        stmt.executeUpdate(deleteTriggerSQL);
    }

    public void insertContact(String tableName, String name, String phone, String email, String address, int age, String company) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{ call sp_insert_contact(?, ?, ?, ?, ?, ?, ?) }")) {
            cs.setString(1, tableName);
            cs.setString(2, name);
            cs.setString(3, phone);
            cs.setString(4, email);
            cs.setString(5, address);
            cs.setInt(6, age);
            cs.setString(7, company);
            cs.executeUpdate();
        }
    }

    public void updateContact(String tableName, int id, String name, String phone, String email, String address, int age, String company) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{ call sp_update_contact(?, ?, ?, ?, ?, ?, ?, ?) }")) {
            cs.setString(1, tableName);
            cs.setInt(2, id);
            cs.setString(3, name);
            cs.setString(4, phone);
            cs.setString(5, email);
            cs.setString(6, address);
            cs.setInt(7, age);
            cs.setString(8, company);
            cs.executeUpdate();
        }
    }

    public int deleteContact(String tableName, String name, String phone) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{ call sp_delete_contact(?, ?, ?) }")) {
            cs.setString(1, tableName);
            cs.setString(2, name);
            cs.setString(3, phone);
            return cs.executeUpdate();
        }
    }

    public List<Contacts> getContactsList(String tableName) throws SQLException {
        List<Contacts> contacts = new ArrayList<>();
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            cs = con.prepareCall("{ call sp_get_contacts(?) }");
            cs.setString(1, tableName);
            boolean hasResult = cs.execute();
            if (hasResult) {
                rs = cs.getResultSet();
                while (rs.next()) {
                    String n = rs.getString("name");
                    String p = rs.getString("phone");
                    String e = rs.getString("email");
                    String addr = rs.getString("address");
                    int ag = rs.getInt("age");
                    String comp = rs.getString("company");
                    contacts.add(new Contacts(n, p, e, addr, ag, comp));
                }
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
            if (cs != null) try { cs.close(); } catch (SQLException ignore) {}
        }
        return contacts;
    }

    public void dropContactBook(String bookName) throws SQLException {
        try (CallableStatement cs = con.prepareCall("{ call sp_drop_contact_book(?) }")) {
            cs.setString(1, bookName);
            cs.executeUpdate();
        }

        // Drop triggers
        Statement stmt = con.createStatement();
        try {
            stmt.executeUpdate("DROP TRIGGER IF EXISTS trg_after_insert_" + bookName);
            stmt.executeUpdate("DROP TRIGGER IF EXISTS trg_after_delete_" + bookName);
        } catch (SQLException ignore) {
            // Ignore if triggers don't exist
        }
    }

    public List<String> getAllTableNames() throws SQLException {
        List<String> tableNames = new ArrayList<>();
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SHOW TABLES")) {
            while (rs.next()) {
                String tableName = rs.getString(1);
                if (!"BackupLogs".equalsIgnoreCase(tableName)) {
                    tableNames.add(tableName);
                }
            }
        }
        return tableNames;
    }

    public Connection getConnection() {
        return con;
    }

    public void closeConnection() throws SQLException {
        if (con != null && !con.isClosed()) {
            con.close();
        }
    }
}