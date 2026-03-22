# Contact Book Application

A robust, Java-based Command-Line Interface (CLI) application for managing multiple contact directories with a persistent MySQL database backend. Engineered entirely from scratch utilizing custom Data Structures, this application executes lightning-fast string searches, operation history tracking, and extensive bulk data importing/exporting seamlessly securely linking Java's in-memory status with backend structural arrays.

---

## 🚀 Key Features

### 1. Custom Engineered Data Structures
Instead of relying purely upon built-in tools, this application successfully leverages core computer science structures manually hardcoded to execute critical logic correctly:
*   **Binary Search Tree (BST):** Guarantees swift `O(log n)` name queries natively equipped to iterate through exact-matches, identifying duplicate contacts smoothly.
*   **Undo Stack Engine:** A bespoke `UndoStack` empowers users with fallback capability; instantly recovering or reversing accidental modifications from the active session safely.
*   **Active Circular Queues:** Provides real-time visibility into an underlying logging system by tracking the most recent interactions seamlessly across application phases. 
*   **Linked Lists:** Sequentially aligns and links dynamically mapped arrays ensuring swift sorts strictly via alphabetical naming architectures or incremental ages.

### 2. Multi-Directory SQL Persistence
*   **Multiple Contact Books:** Generates distinctly segmented environments ("Contact Books") seamlessly orchestrating unique table parameters utilizing MySQL servers on-the-fly. 
*   **Password Authenticated Operations:** Blocks destructive configurations with critical authentication gateways explicitly.

### 3. File Interoperability (CSV & TXT Support)
*   **Bulk Document Exporting:** Swiftly processes comprehensive database strings translating structural formatting directly outward successfully to raw `.CSV` and `.TXT` textual assets on demand.
*   **Bulk Database Importer:** Integrates powerful custom string-parsers dynamically converting raw textual artifacts sequentially backward securely inside remote storage tables natively! (Stored natively heavily inside the local partition `D://ContactBookFiles/`).

---

## 🛠️ Technology Stack
*   **Language:** Java (JDK / Standard Edition)
*   **Database Management:** MySQL Server 
*   **Connectors:** JDBC API (`mysql-connector-j-9.3.0.jar`)
*   **Ideology:** Object-Oriented Design (OOP) and Native Memory Architectures

---

## 🗂️ Codebase Architecture Structure
*   `MainApplication.java`: The central core. Connects UI-looped interactions natively navigating directories explicitly mapping ID references inside mapped user flows. 
*   `DBManager.java`: Enforces the relational SQL parameters directly managing secure update queries, insertions triggers, modifications, and seamless JDBC routing sequences.
*   `DS.java`: Houses all explicitly defined structural memory blocks (`BST`, primitive `Node` entities, arrays bounding `UndoStack` limits, queue iterators `CircularQueue`, and Object schemas `ContactBook` / `Contacts`).
*   `JavaUtils.java`: The primary utility suite encapsulating input sanitization boundaries, RegEx validations, document I/O protocols, and parsing structures.

---

## 🏁 Getting Started

### Prerequisites:
1. **Java Development Kit (JDK):** Version 8+ must be explicitly installed.
2. **MySQL Database Server:** An active MySQL local server operating on port `3306`.
3. **JDBC Connector:** The dependency `.jar` (`mysql-connector-j-9.3.0`) loaded into native Project Build Paths completely.

### Database Setup:
Before spinning up the local application securely, a core database instance manually titled `contactbookdb` explicitly must be generated natively within MySQL architectures:
```sql
CREATE DATABASE contactbookdb;
```
Ensure you have updated your local `DBManager.java` URL credentials (username / passwords) matching respective local machines natively.

### Running the Application:
Once dependencies natively compile correctly, securely execute `MainApplication.java`. It will safely boot up a terminal user interface securely mapping directory assets!

> **Note:** The application inherently relies on generating physical storage directories locally across drives (`D://ContactBooks/` and `D://ContactBookFiles/`). Ensure respective Windows execution permissions validate correctly upon installation!
