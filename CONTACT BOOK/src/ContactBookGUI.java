package MyProject.Project_With_GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class ContactBookGUI extends JFrame {

    // Node structure for linked list
    static class ContactNode {
        String name;
        String phone;
        ContactNode next;

        ContactNode(String name, String phone) {
            this.name = name;
            this.phone = phone;
            this.next = null;
        }
    }


    // Contact book logic
    static class ContactBook {
        ContactNode head;
        private File contactFile;

        public ContactBook(File file) {
            this.contactFile = file;
            loadFromFile();
        }


        public void addContact(String name, String phone) {
            ContactNode newNode = new ContactNode(name, phone);
            if (head == null) {
                head = newNode;
            }
            else {
                ContactNode temp = head;
                while (temp.next != null) {
                    temp = temp.next;
                }
                temp.next = newNode;
            }
            saveToFile();
        }


        public String viewContacts() {
            if (head == null) return "The contact book is empty!";

            StringBuilder sb = new StringBuilder("Your contacts:\n");
            ContactNode temp = head;
            while (temp != null) {
                sb.append("Name: ").append(temp.name).append(" | Phone: ").append(temp.phone).append("\n");
                temp = temp.next;
            }
            return sb.toString();
        }


        public String searchContact(String name) {
            ContactNode temp = head;
            while (temp != null) {
                if (temp.name.equalsIgnoreCase(name)) {
                    return "Found: " + temp.name + " | " + temp.phone;
                }
                temp = temp.next;
            }
            return "Contact not found.";
        }


        public String deleteContact(String name) {
            if (head == null) return "Contact list is empty.";

            if (head.name.equalsIgnoreCase(name)) {
                head = head.next;
                saveToFile();
                return "Contact deleted.";
            }

            ContactNode prev = null, curr = head;
            while (curr != null && !curr.name.equalsIgnoreCase(name)) {
                prev = curr;
                curr = curr.next;
            }

            if (curr == null) return "Contact not found.";
            prev.next = curr.next;
            saveToFile();
            return "Contact deleted.";
        }


        public String updateContact(String name, String newPhone) {
            ContactNode temp = head;
            while (temp != null) {
                if (temp.name.equalsIgnoreCase(name)) {
                    temp.phone = newPhone;
                    saveToFile();
                    return "Contact updated.";
                }
                temp = temp.next;
            }
            return "Contact not found.";
        }


        private void saveToFile() {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(contactFile))) {
                ContactNode temp = head;

                while (temp != null) {
                    bw.write(temp.name + "," + temp.phone);
                    bw.newLine();
                    temp = temp.next;
                }
            }
            catch (IOException e) {
                System.err.println("Error saving contacts: " + e.getMessage());
            }
        }


        private void loadFromFile() {
            if (!contactFile.exists()) return;

            try (BufferedReader br = new BufferedReader(new FileReader(contactFile))) {
                String line;

                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",", 2);
                    if (parts.length == 2) {
                        addContactWithoutSaving(parts[0], parts[1]);
                    }
                }
            }
            catch (IOException e) {
                System.err.println("Error loading contacts: " + e.getMessage());
            }
        }


        private void addContactWithoutSaving(String name, String phone) {
            ContactNode newNode = new ContactNode(name, phone);

            if (head == null) {
                head = newNode;
            }

            else {
                ContactNode temp = head;
                while (temp.next != null) {
                    temp = temp.next;
                }
                temp.next = newNode;
            }
        }
    }

    private ContactBook book;

    private final JTextField nameField = new JTextField(15);
    private final JTextField phoneField = new JTextField(15);
    private final JTextArea displayArea = new JTextArea(15, 30);

    public ContactBookGUI() {
        // Ask user for file location
        File selectedFile = promptForFile();

        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "No file selected. Exiting...");
            System.exit(0);
        }

        book = new ContactBook(selectedFile);

        setTitle("Contact Book");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input Fields
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Contact Info"));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Phone:"));
        inputPanel.add(phoneField);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        JButton addButton = new JButton("Add");
        JButton viewButton = new JButton("View");
        JButton searchButton = new JButton("Search");
        JButton deleteButton = new JButton("Delete");
        JButton updateButton = new JButton("Update");
        JButton clearButton = new JButton("Clear");

        buttonPanel.add(addButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(clearButton);

        // Display Area
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Button actions
        addButton.addActionListener(e -> handleAdd());
        viewButton.addActionListener(e -> displayArea.setText(book.viewContacts()));
        searchButton.addActionListener(e -> handleSearch());
        deleteButton.addActionListener(e -> handleDelete());
        updateButton.addActionListener(e -> handleUpdate());
        clearButton.addActionListener(e -> clearFields());

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private File promptForFile() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose or create a file to store contacts");
        fileChooser.setSelectedFile(new File("contacts.txt"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    private void handleAdd() {

        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (!name.isEmpty() && !phone.isEmpty()) {
            book.addContact(name, phone);
            displayArea.setText("Contact added: " + name + " | " + phone);
            clearFields();
        }
        else {
            displayArea.setText("Please enter both name and phone.");
        }
    }

    private void handleSearch() {

        String name = nameField.getText().trim();

        if (!name.isEmpty()) {
            displayArea.setText(book.searchContact(name));
        }
        else {
            displayArea.setText("Please enter a name to search.");
        }
    }

    private void handleDelete() {
        String name = nameField.getText().trim();

        if (!name.isEmpty()) {
            displayArea.setText(book.deleteContact(name));
            clearFields();
        }
        else {
            displayArea.setText("Please enter a name to delete.");
        }
    }


    private void handleUpdate() {

        String name = nameField.getText().trim();

        String phone = phoneField.getText().trim();

        if (!name.isEmpty() && !phone.isEmpty()) {
            displayArea.setText(book.updateContact(name, phone));
            clearFields();
        }
        else {

            displayArea.setText("Enter both name and new phone to update.");

        }
    }


    private void clearFields() {
        nameField.setText("");
        phoneField.setText("");

    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(ContactBookGUI::new);
    }
}
