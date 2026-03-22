import java.io.*;
import java.sql.*;
import java.util.*;

// utility class for file operations and validation
public class JavaUtils {
    
    // create directory if it doesn't exist
    public static void createDirectory(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
    }
    
    // get all CSV files from a folder
    public static File[] getCSVFiles(String folderPath) {
        File folder = new File(folderPath);
        return folder.listFiles((dir, nameFile) -> nameFile.toLowerCase().endsWith(".csv"));
    }
    
    // get all TXT files from a folder
    public static File[] getTXTFiles(String folderPath) {
        File folder = new File(folderPath);
        return folder.listFiles((dir, nameFile) -> nameFile.toLowerCase().endsWith(".txt"));
    }
    
    // display list of files to user
    public static void displayFileList(File[] files, String fileType) {
        if (files == null || files.length == 0) {
            System.out.println("No " + fileType + " files found in D://ContactBookFiles/");
            return;
        }
        
        System.out.println("Available " + fileType + " files:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }
    }
    
    // import contacts from CSV file
    public static int importCSVFile(File csvFile, ContactBook cb, String tableName, 
                                    DBManager dbManager, UndoStack undoStack, 
                                    CircularQueue recentAdded, Scanner sc) {
        System.out.println("Importing from: " + csvFile.getAbsolutePath());
        System.out.print("Does the file have a header? (y/n): ");
        String header = sc.nextLine().trim();
        
        int imported = 0, skipped = 0, lineNo = 0;
        List<String> importedContacts = new ArrayList<>(); // store all contacts for batch undo
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            
            // skip header if present
            if (header.equalsIgnoreCase("y")) {
                reader.readLine();
                lineNo++;
            }
            
            // read each line and parse contact data
            while ((line = reader.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) continue;
                
                // split by comma, handling quoted fields
                String[] data = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                if (data.length != 6) {
                    skipped++;
                    continue;
                }
                
                // clean up data fields
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].trim().replaceAll("^\"|\"$", "");
                }
                
                String name = data[0], phone = data[1], email = data[2], 
                       address = data[3], company = data[5];
                int age;
                
                try {
                    age = Integer.parseInt(data[4]);
                } catch (NumberFormatException ex) {
                    skipped++;
                    continue;
                }
                
                // create contact and add to memory and database
                Contacts ct = new Contacts(name, phone, email, address, age, company);
                cb.insertRear(ct);
                
                try {
                    dbManager.insertContact(tableName, name, phone, email, address, age, company);
                    importedContacts.add(name + "," + phone + "," + email + "," + address + "," + age + "," + company); // store for batch undo
                    recentAdded.enqueue(name + " - " + phone);
                    imported++;
                } catch (SQLException ex) {
                    System.out.println("Error inserting contact: " + ex.getMessage());
                    skipped++;
                }
            }
            
            // save batch undo operation for all imported contacts
            if (!importedContacts.isEmpty()) {
                String batchData = String.join("|", importedContacts) + "," + tableName;
                undoStack.push("BATCH_ADD", batchData);
                System.out.println("Batch undo operation saved for " + imported + " contacts");
            }
            
            System.out.println("CSV import complete. Imported: " + imported + ", Skipped: " + skipped);
        } catch (Exception e) {
            System.out.println("Error reading CSV (line " + lineNo + "): " + e.getMessage());
        }
        
        return imported;
    }
    
    // import contacts from TXT file
    public static int importTXTFile(File txtFile, ContactBook cb, String tableName, 
                                    DBManager dbManager, UndoStack undoStack, 
                                    CircularQueue recentAdded) {
        System.out.println("Importing from: " + txtFile.getAbsolutePath());
        int imported = 0, skipped = 0, lineNo = 0;
        List<String> importedContacts = new ArrayList<>(); // store all contacts for batch undo
        
        try (BufferedReader br = new BufferedReader(new FileReader(txtFile))) {
            String line;
            
            // read each line and parse contact data
            while ((line = br.readLine()) != null) {
                lineNo++;
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] data = line.split(",");
                if (data.length != 6) {
                    skipped++;
                    continue;
                }
                
                // clean up data fields
                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].trim();
                }
                
                String name = data[0], phone = data[1], email = data[2], 
                       address = data[3], company = data[5];
                int age;
                
                try {
                    age = Integer.parseInt(data[4]);
                } catch (NumberFormatException ex) {
                    skipped++;
                    continue;
                }
                
                // create contact and add to memory and database
                Contacts contact = new Contacts(name, phone, email, address, age, company);
                cb.insertRear(contact);
                
                try {
                    dbManager.insertContact(tableName, name, phone, email, address, age, company);
                    importedContacts.add(name + "," + phone + "," + email + "," + address + "," + age + "," + company); // store for batch undo
                    recentAdded.enqueue(name + " - " + phone);
                    imported++;
                } catch (SQLException e) {
                    System.out.println("Error inserting contact: " + e.getMessage());
                    skipped++;
                }
            }
            
            // save batch undo operation for all imported contacts
            if (!importedContacts.isEmpty()) {
                String batchData = String.join("|", importedContacts) + "," + tableName;
                undoStack.push("BATCH_ADD", batchData);
                System.out.println("Batch undo operation saved for " + imported + " contacts");
            }
            
            System.out.println("TXT import complete. Imported: " + imported + ", Skipped: " + skipped);
        } catch (Exception e) {
            System.out.println("Error reading TXT (line " + lineNo + "): " + e.getMessage());
        }
        
        return imported;
    }
    
    // export contacts to CSV file
    public static void exportToCSV(ContactBook cb, String tableName) {
        if (cb.getContacts().isEmpty()) {
            System.out.println("No Contacts to Export");
            return;
        }
        
        String exportFilePath = "D://ContactBookFiles/" + tableName + ".csv";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(exportFilePath))) {
            // write header
            writer.write("Name,Phone,Email,Address,Age,Company\n");
            
            // write each contact
            for (Contacts ct : cb.getContacts()) {
                writer.write(ct.name + "," + ct.phoneNumber + "," + ct.email + "," + 
                           ct.residentAddress + "," + ct.Age + "," + ct.CompanyName + "\n");
            }
            System.out.println("Contacts exported to: " + exportFilePath);
        } catch (Exception e) {
            System.out.println("Error writing CSV: " + e.getMessage());
        }
    }
    
    // export contacts to TXT file
    public static void exportToTXT(ContactBook cb, String tableName) {
        if (cb.getContacts().isEmpty()) {
            System.out.println("No contacts available to export.");
            return;
        }
        
        String txtFilePath = "D:\\ContactBookFiles\\" + tableName + ".txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(txtFilePath))) {
            // write each contact on a new line
            for (Contacts ct : cb.getContacts()) {
                bw.write(ct.getName() + "," + ct.getPhoneNumber() + "," + ct.getEmail() + "," + 
                        ct.getResidentAddress() + "," + ct.getAge() + "," + ct.getCompanyName());
                bw.newLine();
            }
            System.out.println("Contacts exported to TXT: " + txtFilePath);
        } catch (Exception e) {
            System.out.println("Error writing TXT: " + e.getMessage());
        }
    }
    
    // validate new contact book name
    public static String validateBookName(Scanner sc, HashMap<String, Integer> Book1) {
        String name;
        while (true) {
            try {
                System.out.println("Enter the name of the Contact book:");
                name = sc.nextLine().trim();
                
                if (name.isEmpty()) {
                    throw new Exception("Name cannot be empty.");
                }
                if (!name.matches("^[a-zA-Z_]+$")) {
                    throw new Exception("Name can only contain letters and underscores.");
                }
                if (Book1.containsKey(name)) {
                    throw new Exception("Contact book already exists by this name.");
                }
                break;
            } catch (Exception e) {
                System.out.println("Invalid name: " + e.getMessage());
            }
        }
        return name;
    }
    
    // validate existing contact book name for modification
    public static String validateExistingBookName(Scanner sc, HashMap<String, Integer> Book1) {
        String name;
        while (true) {
            try {
                System.out.println("Enter the name of the Contact book to modify:");
                name = sc.nextLine().trim();
                
                if (name.isEmpty()) {
                    throw new Exception("Name cannot be empty.");
                }
                if (!name.matches("^[a-zA-Z_]+$")) {
                    throw new Exception("Name can only contain letters and underscores.");
                }
                // check if book exists (for modification)
                if (Book1.containsKey(name)) {
                    break; // book exists, so we can modify it
                } else {
                    throw new Exception("No contact book exists with this name.");
                }
            } catch (Exception e) {
                System.out.println("Invalid name: " + e.getMessage());
            }
        }
        return name;
    }
    
    // validate contact name input
    public static String validateContactName(Scanner sc) {
        String cname;
        while (true) {
            try {
                System.out.println("Enter Name : ");
                cname = sc.nextLine().trim();
                
                if (cname.isEmpty()) {
                    throw new Exception("Name cannot be empty.");
                }
                if (!cname.matches("^[a-zA-Z_]+$")) {
                    throw new Exception("Name can only contain letters and underscores.");
                }
                break;
            } catch (Exception e) {
                System.out.println("Invalid name: " + e.getMessage());
            }
        }
        return cname;
    }
    
    // validate phone number input
    public static String validatePhoneNumber(Scanner sc) {
        String phone;
        while (true) {
            try {
                System.out.println("Enter phone number :- ");
                phone = sc.nextLine();
                long num = Long.parseLong(phone);
                
                // check if phone starts with 6,7,8,9 and is 10 digits
                if ((phone.startsWith("6") || phone.startsWith("7") || 
                     phone.startsWith("8") || phone.startsWith("9")) && phone.length() <= 10) {
                    break;
                } else {
                    System.out.println("Phone number length should be of 10 digits only and start with 6,7,8 or 9");
                }
            } catch (Exception e) {
                System.out.println("No special characters or alphabets were allowed in this field");
            }
        }
        return phone;
    }
    
    // validate email input
    public static String validateEmail(Scanner sc) {
        String email;
        while (true) {
            try {
                System.out.print("Enter email: ");
                email = sc.nextLine().trim();
                
                // check email format and allowed domains
                if (!email.matches("^[a-zA-Z0-9._%+-]+@(gmail\\.com|yahoo\\.com|hotmail\\.com|outlook\\.com)$")) {
                    throw new Exception("Email must be valid and end with gmail.com, yahoo.com, hotmail.com, or outlook.com");
                }
                break;
            } catch (Exception e) {
                System.out.println("Invalid email: " + e.getMessage());
            }
        }
        return email;
    }
    
    // validate age input
    public static int validateAge(Scanner sc) {
        int age;
        while (true) {
            try {
                System.out.print("Enter new Age (numbers only): ");
                String ageInput = sc.nextLine();
                
                if (ageInput.matches("^[0-9]+$")) {
                    age = Integer.parseInt(ageInput);
                    if (age > 0 && age <= 120) break;
                }
                System.out.println("Invalid age. Must be numeric (1–120).");
            } catch (Exception e) {
                System.out.println("Invalid age input.");
            }
        }
        return age;
    }
    
    // validate search name input
    public static String validateSearchName(Scanner sc) {
        String search;
        while (true) {
            try {
                System.out.print("Enter the name to search: ");
                search = sc.nextLine().trim();
                
                if (search.isEmpty()) {
                    throw new Exception("Search name cannot be empty.");
                }
                if (!search.matches("^[a-zA-Z ]+$")) {
                    throw new Exception("Search name can only contain letters and spaces.");
                }
                break;
            } catch (Exception e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }
        return search;
    }
    
    // get integer input from user
    public static int promptInt(Scanner sc, String prompt) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                String input = sc.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    // get integer input within a range
    public static int promptIntInRange(Scanner sc, String prompt, int min, int max) {
        while (true) {
            int val = promptInt(sc, prompt);
            if (val >= min && val <= max) return val;
            System.out.println("Please enter a number between " + min + " and " + max + ".");
        }
    }
    
    // perform undo operation
    public static void performUndo(UndoStack undoStack, DBManager dbManager, ContactBook cb) {
        String lastOp = undoStack.getLastOperation();
        String lastData = undoStack.getLastData();
        
        if (lastOp == null || lastData == null) {
            System.out.println("Nothing to undo.");
            return;
        }
        
        try {
            if (lastOp.equals("BATCH_ADD")) {
                handleBatchUndo(lastData, dbManager, cb);
            } else {
                handleIndividualUndo(lastOp, lastData, dbManager, cb);
            }
            
            undoStack.pop();
        } catch (Exception e) {
            System.out.println("Undo error: " + e.getMessage());
        }
    }
    
    // handle batch undo for multiple imported contacts
    private static void handleBatchUndo(String lastData, DBManager dbManager, ContactBook cb) {
        String[] parts = lastData.split(",");
        if (parts.length < 2) {
            System.out.println("Batch undo data incomplete.");
            return;
        }
        
        String table = parts[parts.length - 1];
        String contactsData = lastData.substring(0, lastData.lastIndexOf(","));
        
        String[] contacts = contactsData.split("\\|");
        int removedCount = 0;
        
        // remove from database AND in-memory list
        for (String contact : contacts) {
            String[] contactFields = contact.split(",");
            if (contactFields.length >= 2) {
                String name = contactFields[0];
                String phone = contactFields[1];
                
                try {
                    // remove from database
                    PreparedStatement pst = dbManager.getConnection().prepareStatement(
                        "DELETE FROM " + table + " WHERE name = ? AND phone = ?");
                    pst.setString(1, name);
                    pst.setString(2, phone);
                    int rowsDeleted = pst.executeUpdate();
                    if (rowsDeleted > 0) {
                        // remove from in-memory contact list
                        Iterator<Contacts> iterator = cb.getContacts().iterator();
                        while (iterator.hasNext()) {
                            Contacts c = iterator.next();
                            if (c.getName().equals(name) && c.getPhoneNumber().equals(phone)) {
                                iterator.remove();
                                break;
                            }
                        }
                        removedCount++;
                    }
                } catch (SQLException e) {
                    System.out.println("Error removing contact " + name + ": " + e.getMessage());
                }
            }
        }
        
        System.out.println("BATCH UNDO: Removed " + removedCount + " contacts from batch import");
    }
    
    // handle individual undo operations
    private static void handleIndividualUndo(String lastOp, String lastData, DBManager dbManager, ContactBook cb) {
        String[] data = lastData.split(",");
        if (data.length < 7) {
            System.out.println("Undo data incomplete.");
            return;
        }
        
        String table = data[6];
        
        if (lastOp.equals("ADD")) {
            try {
                // remove contact from database
                PreparedStatement pst = dbManager.getConnection().prepareStatement(
                    "DELETE FROM " + table + " WHERE name = ? AND phone = ?");
                pst.setString(1, data[0]);
                pst.setString(2, data[1]);
                pst.executeUpdate();
                
                // remove from in-memory list
                Iterator<Contacts> iterator = cb.getContacts().iterator();
                while (iterator.hasNext()) {
                    Contacts c = iterator.next();
                    if (c.getName().equals(data[0]) && c.getPhoneNumber().equals(data[1])) {
                        iterator.remove();
                        break;
                    }
                }
                
                System.out.println("UNDO: Contact removed from database and memory");
            } catch (SQLException e) {
                System.out.println("Error removing contact: " + e.getMessage());
            }
        }
        
        if (lastOp.equals("DELETE_FIRST") || lastOp.equals("DELETE_LAST")) {
            try {
                // restore contact to database
                PreparedStatement pst = dbManager.getConnection().prepareStatement(
                    "INSERT INTO " + table + " (name, phone, email, address, age, company) VALUES (?, ?, ?, ?, ?, ?)");
                pst.setString(1, data[0]);
                pst.setString(2, data[1]);
                pst.setString(3, data[2]);
                pst.setString(4, data[3]);
                pst.setInt(5, Integer.parseInt(data[4]));
                pst.setString(6, data[5]);
                pst.executeUpdate();
                
                // restore to in-memory list
                Contacts restoredContact = new Contacts(data[0], data[1], data[2], data[3], 
                                                    Integer.parseInt(data[4]), data[5]);
                cb.insertRear(restoredContact);
                
                System.out.println("UNDO: Contact restored to database and memory");
            } catch (SQLException e) {
                System.out.println("Error restoring contact: " + e.getMessage());
            }
        }
    }
    
    // show undo stack status
    public static void showUndoStackStatus(UndoStack undoStack) {
        if (undoStack.top == -1) {
            System.out.println("No operations available for undo.");
        } else {
            System.out.println("Undo stack status:");
            System.out.println("  - Operations available: " + (undoStack.top + 1));
            System.out.println("  - Last operation: " + undoStack.getLastOperation());
            
            if (undoStack.getLastOperation() != null && undoStack.getLastOperation().equals("BATCH_ADD")) {
                String lastData = undoStack.getLastData();
                if (lastData != null) {
                    String contactsData = lastData.substring(0, lastData.lastIndexOf(","));
                    String[] contacts = contactsData.split("\\|");
                    System.out.println("  - Last batch import: " + contacts.length + " contacts");
                }
            }
        }
    }
    
    // validate password for contact book modification
    public static boolean validatePassword(Scanner sc) {
        String correctPassword = "contact123";
        int attempts = 3;
        
        try {
            System.out.println("\n=== PASSWORD REQUIRED FOR MODIFICATION ===");
            
            while (attempts > 0) {
                try {
                    System.out.print("Enter password to modify contact book: ");
                    String enteredPassword = sc.nextLine().trim();
                    
                    if (enteredPassword.equals(correctPassword)) {
                        System.out.println("Password correct! Access granted.");
                        return true;
                    } else {
                        attempts--;
                        if (attempts > 0) {
                            System.out.println("Incorrect password. " + attempts + " attempts remaining.");
                        } else {
                            System.out.println("Access denied. Too many failed attempts.");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error reading password input: " + e.getMessage());
                    attempts--;
                    if (attempts > 0) {
                        System.out.println("Please try again. " + attempts + " attempts remaining.");
                    } else {
                        System.out.println("Access denied due to input errors.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Critical error in password validation: " + e.getMessage());
            System.out.println("Access denied for security reasons.");
        }
        
        return false;
    }
}