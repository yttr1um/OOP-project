import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class PantryDashboard extends JFrame {

    private JButton logoutBtn, searchBtn, allBtn, expiringBtn, lowBtn;
    private JButton addBtn, editBtn, deleteBtn, consumeBtn, restockBtn, generateShoppingListBtn;
    private JTextField searchField;
    private JTable table;
    private DefaultTableModel model;
    private ArrayList<Object[]> allItems = new ArrayList<>();
    private ArrayList<Object[]> currentView = new ArrayList<>();

    private String currentUserId;

    String[] columns = { "ID", "Name", "Category", "Quantity", "Unit", "Threshold", "Expiry Date" };

    public PantryDashboard(String name, String userId) {
        this.currentUserId = userId;

        setTitle("Pantry Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel header = createHeader(name);
        JPanel filterPanel = createFilterSection();

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addBtn = new JButton("Add");
        editBtn = new JButton("Edit");
        deleteBtn = new JButton("Delete");
        consumeBtn = new JButton("Consume");
        restockBtn = new JButton("Restock");
        generateShoppingListBtn = new JButton("Generate Shopping List");
        actionPanel.add(addBtn);
        actionPanel.add(editBtn);
        actionPanel.add(deleteBtn);
        actionPanel.add(consumeBtn);
        actionPanel.add(restockBtn);
        actionPanel.add(generateShoppingListBtn);

        // event listeners
        addBtn.addActionListener(e -> addItem());
        deleteBtn.addActionListener(e -> deleteItem());
        editBtn.addActionListener(e -> editItem());
        searchBtn.addActionListener(e -> filterTable());
        allBtn.addActionListener(e -> showAll());
        lowBtn.addActionListener(e -> filterLowStock());
        expiringBtn.addActionListener(e -> filterExpiring());
        consumeBtn.addActionListener(e -> consumeItem());
        restockBtn.addActionListener(e -> restockItem());
        generateShoppingListBtn.addActionListener(e -> openShoppingList());

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);

        loadItems();

        setVisible(true);
    }

    public ArrayList<Object[]> getAllItems() {
        return allItems;
    }

    public DefaultTableModel getModel() {
        return model;
    }

    private JPanel createHeader(String name) {
        JPanel header = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome, " + name);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 0));

        logoutBtn = new JButton("Log out");
        logoutBtn.setFocusable(false);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginWindow();
        });

        header.add(welcomeLabel, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);
        return header;
    }

    private JPanel createFilterSection() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchField = new JTextField(20);
        searchBtn = new JButton("Search");
        allBtn = new JButton("All");
        expiringBtn = new JButton("About to expire (15d)");
        lowBtn = new JButton("Low stock");
        filterPanel.add(searchField);
        filterPanel.add(searchBtn);
        filterPanel.add(allBtn);
        filterPanel.add(expiringBtn);
        filterPanel.add(lowBtn);
        return filterPanel;
    }

    private void addItem() {
        AddItemWindow win = new AddItemWindow(currentUserId);
        win.setVisible(true); // waits until dialog closes

        Object[] row = win.getCreatedRow();

        if (row != null) {
            allItems.add(row);
            model.addRow(row);
            updateItemsFile();
        }
    }

    private void deleteItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }

        String id = model.getValueAt(selectedRow, 0).toString();

        // Remove from allItems
        allItems.removeIf(row -> row[0].toString().equals(id));

        // Remove from table
        model.removeRow(selectedRow);

        updateItemsFile();
    }

    private void editItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to edit.");
            return;
        }

        Object[] row = currentView.get(selectedRow); // get correct row

        EditItemWindow editWin = new EditItemWindow(row[1].toString(), row[2].toString(), (int) row[3],
                row[4].toString(), (int) row[5], row[6].toString());

        editWin.setVisible(true);
        Object[] updated = editWin.getEditedRow();

        if (updated != null) {
            row[1] = updated[0];
            row[2] = updated[1];
            row[3] = updated[2];
            row[4] = updated[3];
            row[5] = updated[4];
            row[6] = updated[5];
            // update table display
            for (int col = 1; col <= 6; col++)
                model.setValueAt(row[col], selectedRow, col);
            updateItemsFile();
        }
    }

    private void consumeItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an item.");
            return;
        }

        // Get item ID from table (currentView row)
        String id = model.getValueAt(selectedRow, 0).toString();
        Object[] row = findItemById(id); // find actual item in allItems

        if (row == null) {
            JOptionPane.showMessageDialog(this, "Item not found.");
            return;
        }

        try {
            int amount = Integer.parseInt(JOptionPane.showInputDialog("Consume amount:"));
            int qty = (int) row[3];

            if (amount <= 0 || amount > qty) {
                JOptionPane.showMessageDialog(this, "Invalid amount.");
                return;
            }

            qty -= amount;
            row[3] = qty;

            // update the table row (currentView)
            model.setValueAt(qty, selectedRow, 3);

            updateItemsFile();

        } catch (Exception ignored) {}
    }


    private void restockItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an item.");
            return;
        }

        String id = model.getValueAt(selectedRow, 0).toString();
        Object[] row = findItemById(id);

        if (row == null) {
            JOptionPane.showMessageDialog(this, "Item not found.");
            return;
        }

        try {
            int amount = Integer.parseInt(JOptionPane.showInputDialog("Restock amount:"));
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Invalid amount.");
                return;
            }

            row[3] = (int) row[3] + amount;

            // update table row (currentView)
            model.setValueAt(row[3], selectedRow, 3);

            updateItemsFile();

        } catch (Exception ignored) {}
    }


    public void updateItemsFile() {
        File file = new File("items.txt");
        ArrayList<String> otherUsersLines = getStrings(file, currentUserId);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String line : otherUsersLines) {
                writer.write(line);
                writer.newLine();
            }

            for (Object[] row : allItems) {
                String expiry = (row[6] == null || row[6].toString().isEmpty()) ? "-" : row[6].toString();

                writer.write(currentUserId + " " + row[0] + " " + row[1] + " " + row[2] + " " +
                        row[3] + " " + row[4]
                        + " " + row[5] + " " + expiry);
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Failed to save items: " + e.getMessage());
        }
    }

    private static ArrayList<String> getStrings(File file, String currentUserId) {
        ArrayList<String> otherUsersLines = new ArrayList<>();

        // Read existing file and keep lines that do NOT belong to currentUserId
        if (file.exists()) {
            try (Scanner reader = new Scanner(file)) {
                while (reader.hasNextLine()) {
                    String line = reader.nextLine().trim();
                    if (line.isEmpty()) {
                        continue;
                    }

                    String[] parts = line.split(" ");
                    if (parts.length < 8) {
                        continue; // skip malformed
                    }
                    String uid = parts[0];
                    if (!uid.equals(currentUserId)) {
                        otherUsersLines.add(line);
                    }
                }
            } catch (IOException _) {
            }
        }
        return otherUsersLines;
    }

    private void loadItems() {
        try (Scanner reader = new Scanner(new File("items.txt"))) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] p = line.split(" ");
                if (p.length < 7) {
                    continue; // malformed
                }

                String expiry = "";
                if (p.length >= 8) {
                    expiry = p[7];
                }

                String userIdFromFile = p[0];
                if (!userIdFromFile.equals(currentUserId)) {
                    continue; // only load current user
                }

                Object[] row = { p[1], // itemId
                        p[2], // name
                        p[3], // category
                        Integer.parseInt(p[4]), // qty
                        p[5], // unit
                        Integer.parseInt(p[6]), // threshold
                        expiry // expiry (may be empty)
                };

                allItems.add(row);
                model.addRow(row);
            }
        } catch (IOException ignored) {
        }
    }

    private void filterTable() {
        setTableData(ItemFilters.filterBySearch(allItems, searchField.getText()));
    }

    private void showAll() {
        setTableData(new ArrayList<>(allItems));
    }

    private void filterLowStock() {
        setTableData(ItemFilters.filterLowStock(allItems));
    }

    private void filterExpiring() {
        setTableData(ItemFilters.filterExpiring(allItems, 15));
    }

    private void openShoppingList() {
        ArrayList<Object[]> lowStock = new ArrayList<>();

        for (Object[] row : allItems) {
            int qty = (int) row[3];
            int threshold = (int) row[5];
            if (qty <= threshold)
                lowStock.add(row);
        }

        new ShoppingListWindow(lowStock, currentUserId, this);
    }

    private void setTableData(ArrayList<Object[]> list) {
        currentView = list; // store currently visible rows
        model.setRowCount(0);
        for (Object[] row : list)
            model.addRow(row);
    }

    private Object[] findItemById(String id) {
        for (Object[] row : allItems) {
            if (row[0].toString().equals(id)) {
                return row;
            }
        }
        return null;
    }
}