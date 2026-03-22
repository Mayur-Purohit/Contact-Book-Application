<p align="center">
  <h1 align="center">📒 Contact Book Application</h1>
  <p align="center">
    A robust, Java-based CLI application for managing multiple contact directories<br/>with a persistent MySQL database backend and custom-built data structures.
  </p>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/JDBC-007396?style=for-the-badge&logo=java&logoColor=white" alt="JDBC"/>
  <img src="https://img.shields.io/badge/CLI-000000?style=for-the-badge&logo=windowsterminal&logoColor=white" alt="CLI"/>
</p>

---

## 📖 Table of Contents

- [About the Project](#about-the-project)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Project Architecture](#project-architecture)
- [Data Structures Used](#data-structures-used)
- [Database Design](#database-design)
- [Getting Started](#getting-started)
- [Usage](#usage)
- [File Structure](#file-structure)
- [Contributing](#contributing)
- [License](#license)

---

## <a id="about-the-project"></a>📌 About the Project

The **Contact Book Application** is a fully-featured command-line contact management system built with **Core Java** and **MySQL**. Unlike typical CRUD applications, this project is engineered from the ground up with **custom data structures** — Binary Search Trees, Linked Lists, Stacks, and Circular Queues — all hand-coded to handle real-world functionality such as fast search, undo operations, and activity logging.

The application supports **multiple independent contact books**, each mapped to its own dynamically-created MySQL table, with full **import/export support** for `.csv` and `.txt` files.

---

## <a id="key-features"></a>🚀 Key Features

### 📁 Multi-Directory Management
- Create, modify, and delete multiple contact books on-the-fly
- Each contact book is backed by its own MySQL table
- Access contact books by **ID** or **Name**

### 🔍 Fast Search with BST
- Contact names are indexed in a **Binary Search Tree** at runtime
- Supports **duplicate name detection** with phone-number disambiguation
- Achieves O(log n) average-case search performance

### ↩️ Undo Operations
- A custom **UndoStack** records every add/delete action
- Supports **individual undo** (single contact) and **batch undo** (bulk imports)
- Restores data in both the in-memory linked list and the MySQL database simultaneously

### 📊 Recent Activity Tracking
- A **Circular Queue** (capacity: 5) tracks the most recently added and deleted contacts
- Provides a rolling window of activity without manual log management

### 📄 File Import & Export
| Format | Import | Export |
|--------|--------|--------|
| CSV    | ✅ Header-aware parsing with quoted-field support | ✅ With column headers |
| TXT    | ✅ Comma-delimited line parsing | ✅ Clean comma-separated output |

- Bulk imports are auto-tracked for **batch undo**
- Files are read from / written to `D:\ContactBookFiles\`

### 🔐 Password-Protected Operations
- Modification and deletion of contact books require password authentication
- 3-attempt lockout mechanism for security

### ✅ Comprehensive Input Validation
- **Name:** Letters and underscores only (RegEx enforced)
- **Phone:** Must start with 6/7/8/9, max 10 digits
- **Email:** Validated against `gmail.com`, `yahoo.com`, `hotmail.com`, `outlook.com` domains
- **Age:** Numeric only, range 1–120

---

## <a id="technology-stack"></a>🛠️ Technology Stack

| Component       | Technology                           |
|-----------------|--------------------------------------|
| **Language**    | Java (JDK 8+)                        |
| **Database**    | MySQL Server                         |
| **Connectivity**| JDBC (`mysql-connector-j-9.3.0.jar`) |
| **Paradigm**    | Object-Oriented Programming (OOP)    |
| **Build**       | Manual compilation (no Maven/Gradle) |

---

## <a id="project-architecture"></a>🏗 Project Architecture

```
┌──────────────────────────────────────────────────────┐
│                   MainApplication                    │
│            (Entry Point & Menu Controller)            │
├──────────────┬──────────────────┬────────────────────┤
│              │                  │                    │
│    ┌─────────▼────────┐  ┌─────▼──────┐  ┌──────────▼─────────┐
│    │    DBManager      │  │    DS       │  │    JavaUtils        │
│    │ (SQL Operations)  │  │ (BST, Stack │  │ (Validation, I/O,  │
│    │                   │  │  Queue, LL) │  │  File Ops, Undo)   │
│    └─────────┬─────────┘  └────────────┘  └────────────────────┘
│              │
│    ┌─────────▼─────────┐
│    │   MySQL Database   │
│    │  (contactbookdb)   │
│    │ ┌───────────────┐  │
│    │ │ Stored Procs   │  │
│    │ │ Triggers       │  │
│    │ │ BackupLogs     │  │
│    │ └───────────────┘  │
│    └───────────────────┘
└──────────────────────────────────────────────────────┘
```

---

## <a id="data-structures-used"></a>🧠 Data Structures Used

| Data Structure       | Class           | Purpose                                                  |
|----------------------|-----------------|----------------------------------------------------------|
| **Binary Search Tree** | `BST`, `Node` | Fast O(log n) contact search by name; handles duplicates |
| **Linked List**       | `ContactBook`  | Sequential storage of contacts with front/rear insertion |
| **Stack (Array-based)**| `UndoStack`   | Tracks add/delete operations for single & batch undo     |
| **Circular Queue**    | `CircularQueue`| Rolling log of 5 most recent additions and deletions     |

All data structures are **hand-coded from scratch** — no `java.util.Stack` or `java.util.Queue` wrappers are used for core logic.

---

## <a id="database-design"></a>🗄 Database Design

### Stored Procedures
| Procedure                 | Description                        |
|---------------------------|------------------------------------|
| `sp_insert_contact`       | Insert a contact into any table    |
| `sp_update_contact`       | Update a contact by ID             |
| `sp_delete_contact`       | Delete a contact by name & phone   |
| `sp_get_contacts`         | Retrieve all contacts from a table |
| `sp_create_contact_book`  | Dynamically create a contact table |
| `sp_drop_contact_book`    | Drop a contact table               |

### Triggers
For each contact book, two triggers are auto-created:
- `trg_after_insert_<book>` — Logs every insertion to `BackupLogs`
- `trg_after_delete_<book>` — Logs every deletion to `BackupLogs`

### BackupLogs Table
```sql
BackupLogs (
    log_id       INT AUTO_INCREMENT PRIMARY KEY,
    operation_type ENUM('INSERT', 'DELETE'),
    book_name    VARCHAR(100),
    name         VARCHAR(50),
    phone        VARCHAR(15),
    email        VARCHAR(100),
    address      VARCHAR(255),
    age          INT,
    company      VARCHAR(100),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

---

## <a id="getting-started"></a>🏁 Getting Started

### Prerequisites

| Requirement               | Details                                    |
|---------------------------|--------------------------------------------|
| Java Development Kit (JDK)| Version **8** or higher                    |
| MySQL Server              | Running locally on port `3306`             |
| JDBC Connector            | `mysql-connector-j-9.3.0.jar` (included)  |

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/Mayur-Purohit/Contact-Book-Application.git
cd Contact-Book-Application
```

### 2️⃣ Set Up the Database
Open your MySQL client and create the required database:
```sql
CREATE DATABASE ContactBookManagement2;
```

### 3️⃣ Configure Database Credentials
Open `src/DBManager.java` and update the connection details to match your local setup:
```java
private String dburl  = "jdbc:mysql://localhost:3306/ContactBookManagement2";
private String dbuser = "root";
private String dbpass = "";   // ← Set your MySQL password here
```

### 4️⃣ Compile & Run
```bash
# Compile all source files with the JDBC connector on the classpath
javac -cp ".;mysql-connector-j-9.3.0.jar" src/*.java

# Run the application
java -cp ".;src;mysql-connector-j-9.3.0.jar" MainApplication
```

> **Note:** On Linux/macOS, replace `;` with `:` in the classpath separator.

---

## <a id="usage"></a>💡 Usage

### Main Menu
```
1. To Create new Contact Book
2. To Modify existing Contact Book
3. To Delete Contact Book
4. To Display all Contact Books
5. To Exit
```

### Contact Book Operations (after selecting a book)
```
 1. Add Contact              9.  Import CSV file
 2. Update Contact by Name  10.  Export CSV file
 3. Display All Contacts    11.  Import TXT file
 4. Delete First Contact    12.  Export TXT file
 5. Delete Last Contact     13.  UNDO Last Operation
 6. Search by Name (BST)    14.  View Recent Additions
 7. Sort by Name            15.  View Recent Deletions
 8. Sort by Age             16.  Exit to Main Menu
```

### Sample Workflow
```
1 → Create a new Contact Book named "friends"
2 → Modify "friends" → Add a contact → Search by name
9 → Import a CSV file with 100 contacts in bulk
13 → Undo entire bulk import (all 100 removed)
```

---

## <a id="file-structure"></a>📂 File Structure

```
Contact-Book-Application/
│
├── src/
│   ├── MainApplication.java    # Entry point — main menu & workflow orchestration
│   ├── DBManager.java          # JDBC layer — stored procedures, triggers, CRUD
│   ├── DS.java                 # Data structures — BST, Node, UndoStack,
│   │                           #   CircularQueue, ContactBook, Contacts
│   └── JavaUtils.java          # Utilities — validation, file I/O, undo logic
│
├── mysql-connector-j-9.3.0.jar # JDBC driver (kept because project has no build tool)
├── .gitignore
└── README.md
```

---

## <a id="contributing"></a>🤝 Contributing

Contributions are welcome! To get started:

1. **Fork** this repository
2. **Create** a feature branch: `git checkout -b feature/your-feature`
3. **Commit** your changes: `git commit -m "Add your feature"`
4. **Push** to the branch: `git push origin feature/your-feature`
5. **Open** a Pull Request

---

## <a id="license"></a>📜 License

This project is open source and available for educational purposes.

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/Mayur-Purohit">Mayur Purohit</a>
</p>
