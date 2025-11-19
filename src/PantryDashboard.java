import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class PantryDashboard extends JFrame {

    private JButton searchBtn;
    private JButton allBtn;
    private JButton expiringBtn;
    private JButton lowBtn;
    private JLabel reportLabel, headerLabel;
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
        table.setDefaultRenderer(Object.class, new ExpiryRowRenderer());
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton deleteBtn = new JButton("Delete");
        JButton consumeBtn = new JButton("Consume");
        JButton restockBtn = new JButton("Restock");
        JButton generateShoppingListBtn = new JButton("Generate Shopping List");
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

        // Report panel (bottom small window)
        // Create a styled report panel (centered, colored)
        JPanel reportPanel = new JPanel();
        reportPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        reportPanel.setBackground(null);

        reportLabel = new JLabel("Items: 0 | Low Stock: 0 | Expiring Soon: 0");
        reportLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        reportPanel.add(reportLabel);

        // Wrap report panel ABOVE action buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());

        bottomPanel.add(reportPanel, BorderLayout.NORTH);  // Report on top
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);  // Buttons under it

        add(bottomPanel, BorderLayout.SOUTH);


        add(bottomPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

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
        headerLabel = new JLabel("Welcome, " + name);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 0));

        JButton logoutBtn = new JButton("Log out");
        logoutBtn.setFocusable(false);
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginWindow();
        });

        header.add(headerLabel, BorderLayout.WEST);
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
            updateReport();
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
        updateReport();
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
            updateReport();
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
            updateReport();

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
            updateReport();

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

                writer.write(currentUserId + " | " + row[0] + " | " + row[1] + " | " + row[2] + " | " +
                        row[3] + " | " + row[4]
                        + " | " + row[5] + " | " + expiry);
                writer.newLine();
            }
            updateReport();

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

                    String[] parts = line.split(" \\| ");
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

                String[] p = line.split(" \\| ");
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
                updateReport();
                currentView = new ArrayList<>(allItems);
            }
        } catch (IOException ignored) {
        }
    }

    private void filterTable() {
        String query = searchField.getText();
        setTableData(ItemFilters.filterBySearch(allItems, query));
        headerLabel.setText("Search Results: \"" + query + "\"");
    }

    private void showAll() {
        setTableData(new ArrayList<>(allItems));
        headerLabel.setText("All Items");
    }

    private void filterLowStock() {
        setTableData(ItemFilters.filterLowStock(allItems));
        headerLabel.setText("Low Stock Items");
    }

    private void filterExpiring() {
        setTableData(ItemFilters.filterExpiring(allItems, 15));
        headerLabel.setText("Expiring Soon (<15 Days)");
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

    private void updateReport() {
        int total = allItems.size();

        int lowStock = 0;
        int expiring = 0;

        for (Object[] row : allItems) {
            int qty = (int) row[3];
            int threshold = (int) row[5];

            if (qty <= threshold)
                lowStock++;

            String exp = row[6].toString();
            if (!exp.equals("-") && !exp.isEmpty()) {
                try {
                    java.time.LocalDate expDate = java.time.LocalDate.parse(exp);
                    long days = java.time.temporal.ChronoUnit.DAYS
                            .between(java.time.LocalDate.now(), expDate);
                    if (days >= 0 && days <= 15)
                        expiring++;
                } catch (Exception ignored) {}
            }
        }

        reportLabel.setText("Items: " + total + "   |   Low Stock: " + lowStock + "   |   Expiring Soon: " + expiring);
    }

    private class ExpiryRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {

            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            // Get full row actual index in table model
            int modelRow = table.convertRowIndexToModel(row);

            // Read expiry date
            String expiry = model.getValueAt(modelRow, 6).toString();

            // Default background
            if (!isSelected) {
                component.setBackground(Color.WHITE);
            }

            try {
                int qty = Integer.parseInt(model.getValueAt(modelRow, 3).toString());
                int threshold = Integer.parseInt(model.getValueAt(modelRow, 5).toString());

                if (qty <= threshold) {
                    if (!isSelected)
                        component.setBackground(new Color(255, 220, 180)); // light red/pink
                }
            } catch (Exception ignored) {}

            // Highlight expiring soon
            if (!expiry.equals("-") && !expiry.isEmpty()) {
                try {
                    java.time.LocalDate expDate = java.time.LocalDate.parse(expiry);
                    long days = java.time.temporal.ChronoUnit.DAYS
                            .between(java.time.LocalDate.now(), expDate);

                    if (days >= 0 && days <= 15) {
                        if (!isSelected)
                            component.setBackground(new Color(255, 180, 180)); // light orange
                    }

                } catch (Exception ignored) {}
            }

            return component;
        }
    }


}