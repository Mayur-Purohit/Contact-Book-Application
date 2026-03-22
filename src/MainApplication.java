import java.sql.*;
import java.util.*;
import java.io.File;

public class MainApplication {

    public static void main(String[] args) throws Exception {
        // Initialize scanner for user input
        Scanner sc = new Scanner(System.in);

        // Initialize database manager to handle all database operations
        DBManager dbManager = new DBManager();

        // Create necessary directories
        JavaUtils.createDirectory("D://ContactBooks/");
        JavaUtils.createDirectory("D://ContactBookFiles/");

        // Initialize data structures
        HashMap<Integer, ContactBook> Book = new HashMap<Integer, ContactBook>();
        HashMap<String, Integer> Book1 = new HashMap<>();
        int bookID = 1;
        UndoStack undoStack = new UndoStack();
        CircularQueue recentAdded = new CircularQueue();
        CircularQueue recentDeleted = new CircularQueue();

        // Load existing contact books from database
        List<String> tableNames = dbManager.getAllTableNames();
        for (String tableName : tableNames) {
            ContactBook cb = new ContactBook(tableName);
            Book.put(bookID, cb);
            Book1.put(tableName, bookID);
            bookID++;
        }

        boolean b = true;
        while (b) {
            System.out.println("1. To Create new Contact Book");
            System.out.println("2. To Modify existing Contact Book");
            System.out.println("3. To Delete Contact Book");
            System.out.println("4. To Display all Contact Book");
            System.out.println("5. To Exit");
            System.out.println("-------");
            int choice = JavaUtils.promptIntInRange(sc, "Enter your choice", 1, 5);

            switch (choice) {
                case 1:
                    handleCreateContactBook(sc, Book, Book1, bookID, dbManager);
                    bookID++;
                    break;
                case 2:
                    if (Book.isEmpty()) {
                        System.out.println("No Contact Books available.");
                    } else {
                        try {
                            // Add password validation before allowing modification
                            if (JavaUtils.validatePassword(sc)) {
                                handleModifyContactBook(sc, Book, Book1, dbManager, undoStack, recentAdded, recentDeleted);
                            } else {
                                System.out.println("Cannot access modification menu without correct password.");
                            }
                        } catch (Exception e) {
                            System.out.println("Error during password validation: " + e.getMessage());
                            System.out.println("Returning to main menu for security reasons.");
                        }
                    }
                    break;
                // Replace the existing case 3 in MainApplication.java with this updated version:

                case 3:
                    if (Book.isEmpty()) {
                        System.out.println("No Contact Books available for deletion.");
                    } else {
                        try {
                            // Add password validation before allowing deletion
                            if (JavaUtils.validatePassword(sc)) {
                                handleDeleteContactBook(sc, Book, Book1, dbManager);
                            } else {
                                System.out.println("Cannot access deletion menu without correct password.");
                            }
                        } catch (Exception e) {
                            System.out.println("Error during password validation: " + e.getMessage());
                            System.out.println("Returning to main menu for security reasons.");
                        }
                    }
                    break;
                case 4:
                    displayAllContactBooks(Book);
                    break;
                case 5:
                    b = false;
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice");
            }
        }

        // Close database connection
        dbManager.closeConnection();
        sc.close();
    }

    /**
     * Handles the creation of a new contact book
     * Prompts for book name, validates it, and creates the book in both
     * memory and database
     *
     * @param sc scanner for user input
     * @param Book map of book IDs to ContactBook objects
     * @param Book1 map of book names to book IDs
     * @param bookID the ID to assign to the new book
     * @param dbManager database manager for database operations
     */
    private static void handleCreateContactBook(Scanner sc, HashMap<Integer, ContactBook> Book,
                                                HashMap<String, Integer> Book1, int bookID, DBManager dbManager) {
        try {
            String name = JavaUtils.validateBookName(sc, Book1);

            if (Book1.containsKey(name)) {
                System.out.println("Contact book already exists by this name of ID = " + Book1.get(name));
            } else {
                dbManager.createContactBook(name);
                ContactBook cb = new ContactBook(name);
                Book.put(bookID, cb);
                Book1.put(name, bookID);
                System.out.println("Contact book = " + name + " added with id = " + bookID);
            }
        } catch (SQLException e) {
            System.out.println("Error creating contact book: " + e.getMessage());
        }
    }

    private static void handleModifyContactBook(Scanner sc, HashMap<Integer, ContactBook> Book,
                                                HashMap<String, Integer> Book1, DBManager dbManager,
                                                UndoStack undoStack, CircularQueue recentAdded, CircularQueue recentDeleted) {
        System.out.println("Modify by: 1. ID  2. Name");
        int c = JavaUtils.promptIntInRange(sc, "Enter option", 1, 2);

        int id = -1;
        String tableName = "";

        if (c == 1) {
            id = JavaUtils.promptInt(sc, "Enter Contact Book ID");
            if (Book.containsKey(id)) {
                tableName = Book.get(id).getContactBookName();
            } else {
                System.out.println("Book ID not found");
                return;
            }
        } else if (c == 2) {
            String name1 = JavaUtils.validateExistingBookName(sc, Book1);
            id = Book1.get(name1);
            tableName = name1;
        } else {
            System.out.println("Invalid choice");
            return;
        }

        ContactBook cb = Book.get(id);
        loadContactsFromDatabase(cb, tableName, dbManager);

        boolean modify = true;
        while (modify) {
            System.out.println("Modification going on = " + cb.getContactBookName());
            System.out.println("1. Add Contact");
            System.out.println("2. Update any contact by name");
            System.out.println("3. To display All Contacts");
            System.out.println("4. Delete First Contact");
            System.out.println("5. Delete Last Contact");
            System.out.println("6. Search Contact By Name");
            System.out.println("7. Sort Contacts by Name");
            System.out.println("8. Sort Contacts by Age");
            System.out.println("9. To Import a csv file into Database");
            System.out.println("10. To Export a csv file from Database");
            System.out.println("11. To Import a txt file into database");
            System.out.println("12. To Export a txt file from database");
            System.out.println("13. UNDO Last Operation");
            System.out.println("14. Recent Added");
            System.out.println("15. Recent Deleted");
            System.out.println("16. Exit to Main Menu");
            System.out.print("Enter your choice: ");

            int ch = JavaUtils.promptIntInRange(sc, "Enter your choice", 1, 16);

            switch (ch) {
                case 1:
                    handleAddContact(sc, cb, tableName, dbManager, undoStack, recentAdded);
                    break;
                case 2:
                    handleUpdateContact(sc, cb, tableName, dbManager);
                    break;
                case 3:
                    displayContactsFromDatabase(tableName, dbManager);
                    break;
                case 4:
                    handleDeleteFirstContact(cb, tableName, dbManager, undoStack, recentDeleted);
                    break;
                case 5:
                    handleDeleteLastContact(cb, tableName, dbManager, undoStack, recentDeleted);
                    break;
                case 6:
                    String search = JavaUtils.validateSearchName(sc);
                    cb.displayContactsByName(search);
                    break;
                case 7:
                    cb.sortByName();
                    System.out.println("Contacts sorted by Name:");
                    for (Contacts ct : cb.getContacts()) {
                        System.out.println(ct);
                    }
                    break;
                case 8:
                    cb.sortByAge();
                    System.out.println("Contacts sorted by Age:");
                    for (Contacts ct : cb.getContacts()) {
                        System.out.println(ct);
                    }
                    break;
                case 9:
                    handleCSVImport(sc, cb, tableName, dbManager, undoStack, recentAdded);
                    break;
                case 10:
                    JavaUtils.exportToCSV(cb, tableName);
                    break;
                case 11:
                    handleTXTImport(sc, cb, tableName, dbManager, undoStack, recentAdded);
                    break;
                case 12:
                    JavaUtils.exportToTXT(cb, tableName);
                    break;
                case 13:
                    JavaUtils.performUndo(undoStack, dbManager, cb);
                    break;
                case 14:
                    recentAdded.display();
                    break;
                case 15:
                    recentDeleted.display();
                    break;
                case 16:
                    modify = false;
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    /**
     * Loads all contacts from the database into the contact book object
     * Clears existing contacts first, then populates from database
     *
     * @param cb the ContactBook object to populate
     * @param tableName the database table name
     * @param dbManager database manager for database operations
     */
    private static void loadContactsFromDatabase(ContactBook cb, String tableName, DBManager dbManager) {
        try {
            cb.getContacts().clear();
            List<Contacts> list = dbManager.getContactsList(tableName);
            for (Contacts c : list) cb.insertRear(c);
            System.out.println("Loaded " + cb.getContacts().size() + " contacts from database.");
        } catch (SQLException e) {
            System.out.println("Error loading contacts: " + e.getMessage());
        }
    }

    private static void handleAddContact(Scanner sc, ContactBook cb, String tableName,
                                         DBManager dbManager, UndoStack undoStack, CircularQueue recentAdded) {
        System.out.println("1. Insert from Front");
        System.out.println("2. Insert from Rear");
        System.out.print("Enter option: ");
        int opt = JavaUtils.promptIntInRange(sc, "Enter option", 1, 2);

        String cname = JavaUtils.validateContactName(sc);
        String phone = JavaUtils.validatePhoneNumber(sc);
        String email = JavaUtils.validateEmail(sc);
        System.out.print("Enter address: ");
        String address = sc.nextLine();
        int age = JavaUtils.validateAge(sc);
        System.out.print("Enter company: ");
        String company = sc.nextLine();

        Contacts contact = new Contacts(cname, phone, email, address, age, company);

        if (opt == 1) {
            cb.insertFirst(contact);
            System.out.println("Contact inserted at Front (In-memory)");
        } else if (opt == 2) {
            cb.insertRear(contact);
            System.out.println("Contact inserted at Rear (In-memory)");
        } else {
            System.out.println("Invalid option");
            return;
        }

        try {
            dbManager.insertContact(tableName, cname, phone, email, address, age, company);
            System.out.println("Contact saved in database!");
            recentAdded.enqueue(cname + " - " + phone);
            undoStack.push("ADD", cname + "," + phone + "," + email + "," + address + "," + age + "," + company + "," + tableName);
        } catch (SQLException e) {
            System.out.println("Error saving contact to database: " + e.getMessage());
        }
    }

    private static void handleUpdateContact(Scanner sc, ContactBook cb, String tableName, DBManager dbManager) {
        System.out.println("Update by: 1. ID  2. Name");
        int updateChoice = JavaUtils.promptIntInRange(sc, "Choose update mode", 1, 2);

        String updateQuery = "";
        if (updateChoice == 1) {
            int updateId = JavaUtils.promptInt(sc, "Enter Contact ID to update");
            updateQuery = "SELECT * FROM " + tableName + " WHERE id = " + updateId;
        } else if (updateChoice == 2) {
            System.out.print("Enter Contact Name to update: ");
            String updateName = sc.nextLine();
            updateQuery = "SELECT * FROM " + tableName + " WHERE name = '" + updateName + "'";
        } else {
            System.out.println("Invalid choice.");
            return;
        }

        try {
            Statement updateStmt = dbManager.getConnection().createStatement();
            ResultSet updateRs = updateStmt.executeQuery(updateQuery);

            if (!updateRs.next()) {
                System.out.println("No contact found.");
                return;
            }

            int contactId = updateRs.getInt("id");
            System.out.println("Current Contact: " + updateRs.getString("name") + ", " + updateRs.getString("phone") + ", " + updateRs.getString("email") + ", " + updateRs.getString("address") + ", " + updateRs.getInt("age") + ", " + updateRs.getString("company"));

            String newName = JavaUtils.validateContactName(sc);
            String newPhone = JavaUtils.validatePhoneNumber(sc);
            String newEmail = JavaUtils.validateEmail(sc);
            System.out.print("Enter new Address: ");
            String newAddress = sc.nextLine();
            int newAge = JavaUtils.validateAge(sc);
            System.out.print("Enter new Company: ");
            String newCompany = sc.nextLine();

            dbManager.updateContact(tableName, contactId, newName, newPhone, newEmail, newAddress, newAge, newCompany);
            System.out.println("Contact updated successfully! Trigger logged this update.");

            // Update in-memory contact
            for (Contacts ct : cb.getContacts()) {
                if (ct.getName().equalsIgnoreCase(updateRs.getString("name")) || ct.getPhoneNumber().equals(updateRs.getString("phone"))) {
                    ct.setName(newName);
                    ct.setPhoneNumber(newPhone);
                    ct.setEmail(newEmail);
                    ct.setResidentAddress(newAddress);
                    ct.setAge(newAge);
                    ct.setCompanyName(newCompany);
                    break;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updating contact: " + e.getMessage());
        }
    }

    private static void displayContactsFromDatabase(String tableName, DBManager dbManager) {
        System.out.println("\nContacts in Database for " + tableName + ":");
        try {
            List<Contacts> list = dbManager.getContactsList(tableName);
            if (list.isEmpty()) System.out.println("No contacts available in this contact book.");
            for (Contacts ct : list) {
                System.out.println("Name: " + ct.getName() + ", Phone: " + ct.getPhoneNumber() + ", Email: " + ct.getEmail() + ", Address: " + ct.getResidentAddress() + ", Age: " + ct.getAge() + ", Company: " + ct.getCompanyName());
            }
        } catch (SQLException e) {
            System.out.println("Error displaying contacts: " + e.getMessage());
        }
    }

    private static void handleDeleteFirstContact(ContactBook cb, String tableName,
                                                 DBManager dbManager, UndoStack undoStack, CircularQueue recentDeleted) {
        if (!cb.getContacts().isEmpty()) {
            Contacts firstContact = cb.getContacts().getFirst();
            try {
                int rowsDeleted = dbManager.deleteContact(tableName, firstContact.name, firstContact.phoneNumber);
                if (rowsDeleted > 0) {
                    cb.deleteFirst();
                    System.out.println("First contact deleted.");
                    recentDeleted.enqueue(firstContact.name + " - " + firstContact.phoneNumber);
                    undoStack.push("DELETE_FIRST", firstContact.name + "," + firstContact.phoneNumber + "," + firstContact.email + "," + firstContact.residentAddress + "," + firstContact.Age + "," + firstContact.CompanyName + "," + tableName);
                } else {
                    System.out.println("Could not delete from DB.");
                }
            } catch (SQLException e) {
                System.out.println("Error deleting contact: " + e.getMessage());
            }
        } else {
            System.out.println("No contacts to delete.");
        }
    }

    private static void handleDeleteLastContact(ContactBook cb, String tableName,
                                                DBManager dbManager, UndoStack undoStack, CircularQueue recentDeleted) {
        if (!cb.getContacts().isEmpty()) {
            Contacts lastContact = cb.getContacts().getLast();
            try {
                int rowsDeleted = dbManager.deleteContact(tableName, lastContact.name, lastContact.phoneNumber);
                if (rowsDeleted > 0) {
                    cb.deleteLast();
                    System.out.println("Last contact deleted (DB + memory).");
                    recentDeleted.enqueue(lastContact.name + " - " + lastContact.phoneNumber);
                    undoStack.push("DELETE_LAST", lastContact.name + "," + lastContact.phoneNumber + "," + lastContact.email + "," + lastContact.residentAddress + "," + lastContact.Age + "," + lastContact.CompanyName + "," + tableName);
                } else {
                    System.out.println("Could not delete from DB.");
                }
            } catch (SQLException e) {
                System.out.println("Error deleting contact: " + e.getMessage());
            }
        } else {
            System.out.println("No contacts to delete.");
        }
    }

    private static void handleCSVImport(Scanner sc, ContactBook cb, String tableName,
                                        DBManager dbManager, UndoStack undoStack, CircularQueue recentAdded) {
        File[] csvFiles = JavaUtils.getCSVFiles("D://ContactBookFiles/");
        JavaUtils.displayFileList(csvFiles, "CSV");

        if (csvFiles == null || csvFiles.length == 0) {
            return;
        }

        System.out.print("Enter the number of the CSV file to import: ");
        int fileChoiceCsv = JavaUtils.promptInt(sc, "Enter the number of the CSV file to import");

        if (fileChoiceCsv < 1 || fileChoiceCsv > csvFiles.length) {
            System.out.println("Invalid choice.");
            return;
        }

        File chosenCsv = csvFiles[fileChoiceCsv - 1];
        JavaUtils.importCSVFile(chosenCsv, cb, tableName, dbManager, undoStack, recentAdded, sc);
    }

    private static void handleTXTImport(Scanner sc, ContactBook cb, String tableName,
                                        DBManager dbManager, UndoStack undoStack, CircularQueue recentAdded) {
        File[] txtFiles = JavaUtils.getTXTFiles("D://ContactBookFiles/");
        JavaUtils.displayFileList(txtFiles, "TXT");

        if (txtFiles == null || txtFiles.length == 0) {
            return;
        }

        System.out.print("Enter the number of the TXT file to import: ");
        int fileChoiceTxt = JavaUtils.promptInt(sc, "Enter the number of the TXT file to import");

        if (fileChoiceTxt < 1 || fileChoiceTxt > txtFiles.length) {
            System.out.println("Invalid choice.");
            return;
        }

        File chosenTxt = txtFiles[fileChoiceTxt - 1];
        JavaUtils.importTXTFile(chosenTxt, cb, tableName, dbManager, undoStack, recentAdded);
    }

    private static void handleDeleteContactBook(Scanner sc, HashMap<Integer, ContactBook> Book,
                                                HashMap<String, Integer> Book1, DBManager dbManager) {
        System.out.print("Enter Contact Book name to delete: ");
        String delName = sc.nextLine();

        if (Book1.containsKey(delName)) {
            int delId = Book1.get(delName);
            Book.remove(delId);
            Book1.remove(delName);

            try {
                dbManager.dropContactBook(delName);
                System.out.println("Contact Book deleted successfully from DB.");
            } catch (SQLException e) {
                System.out.println("Error deleting contact book: " + e.getMessage());
            }
        } else {
            System.out.println("No such Contact Book exists.");
        }
    }

    private static void displayAllContactBooks(HashMap<Integer, ContactBook> Book) {
        if (Book.isEmpty()) {
            System.out.println("No Contact Books available.");
        } else {
            System.out.println("Existing Contact Books:");
            for (Map.Entry<Integer, ContactBook> entry : Book.entrySet()) {
                System.out.println("ID: " + entry.getKey() + " | Name: " + entry.getValue().getContactBookName());
            }
        }
    }
}