import java.util.*;

/**
 * Binary Search Tree Node Class
 *
 * This class represents a node in the Binary Search Tree used for
 * efficient contact searching operations. Each node contains a contact
 * and references to left and right child nodes.
 *
 * @author Student Name
 * @version 1.0
 * @since 2025
 */
class Node {
    Contacts key;        // The contact data stored in this node
    Node left, right;    // References to left and right child nodes

    /**
     * Constructor - Creates a new node with the given contact
     *
     * @param item the contact to store in this node
     */
    public Node(Contacts item) {
        key = item;
    }
}

/**
 * Binary Search Tree Class
 *
 * This class implements a Binary Search Tree data structure for
 * efficient contact searching operations. The tree is organized
 * by contact names for fast lookup and retrieval.
 *
 * @author Student Name
 * @version 1.0
 * @since 2025
 */
class BST {
    Node root;    // Root node of the binary search tree

    void insert(Contacts key) {
        root = insertRec(root, key);
    }


    Node insertRec(Node root, Contacts key) {
        if (root == null) {
            return new Node(key);
        }
        int comparison = key.getName().compareToIgnoreCase(root.key.getName());

        if (comparison < 0) {
            root.left = insertRec(root.left, key);
        } else {
            root.right = insertRec(root.right, key);
        }
        return root;
    }

    void searchByNameShowAll(String name) {
        List<Contacts> foundContacts = new ArrayList<>();
        searchByNameRec(root, name, foundContacts);

        if (foundContacts.isEmpty()) {
            System.out.println("No contacts found with name: " + name);
        } else if (foundContacts.size() == 1) {
            System.out.println("Contact found:");
            System.out.println("------------------------");
            printContactDetails(foundContacts.get(0));
            System.out.println("------------------------");
        } else {
            System.out.println("Multiple contacts found with the name '" + name + "'.");
            for (int i = 0; i < foundContacts.size(); i++) {
                System.out.println((i + 1) + ". Name: " + foundContacts.get(i).getName() + " | Phone: " + foundContacts.get(i).getPhoneNumber());
            }
            System.out.println("------------------------");
            System.out.print("Please enter the phone number to get full details: ");
            Scanner sc = new Scanner(System.in);
            String phone = sc.nextLine();

            boolean specificContactFound = false;
            for (Contacts contact : foundContacts) {
                if (contact.getPhoneNumber().equals(phone)) {
                    System.out.println("Details for " + name + " with phone number " + phone + ":");
                    printContactDetails(contact);
                    specificContactFound = true;
                    break;
                }
            }
            if (!specificContactFound) {
                System.out.println("No contact found with that phone number.");
            }
        }
    }

    void searchByNameRec(Node root, String name, List<Contacts> foundContacts) {
        if (root == null) {
            return;
        }
        if (root.key.getName().equalsIgnoreCase(name)) {
            foundContacts.add(root.key);
        }

        searchByNameRec(root.left, name, foundContacts);
        searchByNameRec(root.right, name, foundContacts);
    }

    private void printContactDetails(Contacts contact) {
        System.out.println("Name: " + contact.getName());
        System.out.println("Phone: " + contact.getPhoneNumber());
        System.out.println("Email: " + contact.getEmail());
        System.out.println("Address: " + contact.getResidentAddress());
        System.out.println("Age: " + contact.getAge());
        System.out.println("Company: " + contact.getCompanyName());
    }

    Contacts searchContactByName(String name) {
        return searchRec(root, name);
    }

    Contacts searchRec(Node root, String name) {
        if (root == null) {
            return null;
        }

        int comparison = name.compareToIgnoreCase(root.key.getName());

        if (comparison == 0) {
            return root.key;
        } else if (comparison < 0) {
            return searchRec(root.left, name);
        } else {
            return searchRec(root.right, name);
        }
    }
}


class UndoStack {
    int top = -1, N = 50;
    String[] operations;
    String[] contactData;

    UndoStack() {
        operations = new String[N];
        contactData = new String[N];
    }

    void push(String operation, String data) {
        if (top >= N - 1) {
            System.out.println("Overflow");
        } else {
            top++;
            operations[top] = operation;
            contactData[top] = data;
            System.out.println("Operation saved for undo");
        }
    }

    void pop() {
        if (top == -1) {
            System.out.println("No operations to undo");
        } else {
            System.out.println("Undoing: " + operations[top]);
            System.out.println("Data: " + contactData[top]);
            top--;
        }
    }

    String getLastOperation() {
        if (top == -1) return null;
        return operations[top];
    }

    String getLastData() {
        if (top == -1) return null;
        return contactData[top];
    }
}

/**
 * Circular Queue Class
 *
 * This class implements a circular queue data structure to keep track
 * of recent operations. When the queue is full, it automatically
 * removes the oldest entry to make room for new ones.
 *
 * @author Student Name
 * @version 1.0
 * @since 2025
 */
class CircularQueue {
    int front = -1, rear = -1, size = 5;    // front: front index, rear: rear index, size: queue capacity
    String[] queue = new String[size];       // Array to store the queue elements

    void enqueue(String data) {
        if ((front == 0 && rear == size - 1) || (front == rear + 1)) {
            dequeue();
        }
        if (front == -1) {
            front = 0;
            rear = 0;
        } else if (rear == size - 1) {
            rear = 0;
        } else {
            rear++;
        }
        queue[rear] = data;
    }

    String dequeue() {
        if (front == -1) return null;
        String data = queue[front];
        if (front == rear) {
            front = -1;
            rear = -1;
        } else if (front == size - 1) {
            front = 0;
        } else {
            front++;
        }
        return data;
    }

    void display() {
        if (front == -1) {
            System.out.println("Empty");
            return;
        }
        for (int i = front; i != rear; i = (i + 1) % size) {
            System.out.println(queue[i]);
        }
        System.out.println(queue[rear]);
    }
}

/**
 * Contact Book Class
 *
 * This class represents a contact book that stores contacts using a LinkedList.
 * It provides methods for adding, removing, searching, and sorting contacts.
 * The class also integrates with BST for efficient searching operations.
 *
 * @author Student Name
 * @version 1.0
 * @since 2025
 */
class ContactBook {
    String ContactBookName;                    // Name of the contact book
    LinkedList<Contacts> contacts;             // List to store all contacts in this book

    public ContactBook(String contactBookName) {
        this.ContactBookName = contactBookName;
        this.contacts = new LinkedList<>();
    }

    void addContact(Contacts c) {
        contacts.add(c);
    }

    LinkedList<Contacts> getContacts() {
        return contacts;
    }

    String getContactBookName() {
        return ContactBookName;
    }

    void insertFirst(Contacts c) {
        contacts.addFirst(c);
    }

    void insertRear(Contacts c) {
        contacts.addLast(c);
    }

    boolean deleteFirst() {
        if (!contacts.isEmpty()) {
            contacts.removeFirst();
            return true;
        } else {
            return false;
        }
    }

    boolean deleteLast() {
        if (!contacts.isEmpty()) {
            contacts.removeLast();
            return true;
        } else {
            return false;
        }
    }

    Contacts searchContactByName(String name) {
        BST searchBST = new BST();
        for (Contacts c : contacts) {
            searchBST.insert(c);
        }
        return searchBST.searchContactByName(name);
    }

    void displayContactsByName(String name) {
        BST searchBST = new BST();
        for (Contacts c : contacts) {
            searchBST.insert(c);
        }
        searchBST.searchByNameShowAll(name);
    }

    void sortByName() {
        contacts.sort(Comparator.comparing(Contacts::getName, String.CASE_INSENSITIVE_ORDER));
    }

    void sortByAge() {
        contacts.sort(Comparator.comparingInt(Contacts::getAge));
    }
}


class Contacts {
    String name;                    // Contact's full name
    String phoneNumber;             // Contact's phone number
    String email;                   // Contact's email address
    String residentAddress;         // Contact's residential address
    int Age;                        // Contact's age
    String CompanyName;             // Contact's company name

    public Contacts(String name, String phoneNumber, String email, String residentAddress, int age, String companyName) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.residentAddress = residentAddress;
        Age = age;
        CompanyName = companyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResidentAddress() {
        return residentAddress;
    }

    public void setResidentAddress(String residentAddress) {
        this.residentAddress = residentAddress;
    }

    public int getAge() {
        return Age;
    }

    public void setAge(int age) {
        Age = age;
    }

    public String getCompanyName() {
        return CompanyName;
    }

    public void setCompanyName(String companyName) {
        CompanyName = companyName;
    }

    @Override
    public String toString() {
        return "Contacts{" + "name='" + name + '\'' + ", phoneNumber='" + phoneNumber + '\'' + ", email='" + email + '\'' + ", residentAddress='" + residentAddress + '\'' + ", Age=" + Age + ", CompanyName='" + CompanyName + '\'' + '}';
    }
}