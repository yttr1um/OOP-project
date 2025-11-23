import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class PantryDashboard extends JFrame {

    // UI Components
    private JButton searchButton, allButton, expiringButton, lowStockButton;
    private JLabel reportLabel, headerLabel;
    private JTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;

    // Data
    private ArrayList<Object[]> allItems = new ArrayList<>();
    private ArrayList<Object[]> currentView = new ArrayList<>();

    private final String currentUserId;
    private final ItemManager itemManager;
    private final PantryController controller;

    private static final String[] COLUMN_NAMES = { "ID", "Name", "Category", "Quantity", "Unit", "Threshold",
            "Expiry Date" };

    public PantryDashboard(String userName, String userId) {
        this.currentUserId = userId;
        this.itemManager = new ItemManager(userId);
        this.controller = new PantryController(itemManager, allItems);

        setTitle("Pantry Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top section: header + filters
        JPanel headerPanel = createHeader(userName);
        JPanel filterPanel = createFilterSection();
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.SOUTH);

        // Table setup
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Prevent direct editing
            }
        };
        table = new JTable(tableModel);
        table.setDefaultRenderer(Object.class, new ExpiryRowRenderer(tableModel));
        JScrollPane scrollPane = new JScrollPane(table);

        // Action buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addButton = new JButton("Add");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");
        JButton consumeButton = new JButton("Consume");
        JButton restockButton = new JButton("Restock");
        JButton generateShoppingListButton = new JButton("Generate Shopping List");

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.add(consumeButton);
        actionPanel.add(restockButton);
        actionPanel.add(generateShoppingListButton);

        // Button events
        addButton.addActionListener(e -> addItem());
        editButton.addActionListener(e -> editItem());
        deleteButton.addActionListener(e -> deleteItem());
        consumeButton.addActionListener(e -> consumeItem());
        restockButton.addActionListener(e -> restockItem());
        generateShoppingListButton.addActionListener(e -> openShoppingList());
        searchButton.addActionListener(e -> filterTable());
        allButton.addActionListener(e -> showAll());
        lowStockButton.addActionListener(e -> filterLowStock());
        expiringButton.addActionListener(e -> filterExpiring());

        // Report panel (summary of items)
        JPanel reportPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        reportLabel = new JLabel("Items: 0 | Low Stock: 0 | Expiring Soon: 0");
        reportLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        reportPanel.add(reportLabel);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(reportPanel, BorderLayout.NORTH);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        // Add components to frame
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadItems();
        setVisible(true);
    }

    public DefaultTableModel getModel() {
        return tableModel;
    }

    public ArrayList<Object[]> getAllItems() {
        return allItems;
    }

    /**
     * Save all changes to the items file.
     */
    public void updateItemsFile() {
        itemManager.saveItems(allItems);
    }

    // UI Creation Methods

    private JPanel createHeader(String userName) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerLabel = new JLabel("Welcome, " + userName);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 0));

        JButton logoutButton = new JButton("Log out");
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginWindow();
        });

        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        return headerPanel;
    }

    private JPanel createFilterSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        allButton = new JButton("All");
        expiringButton = new JButton("About to expire (15d)");
        lowStockButton = new JButton("Low stock");

        panel.add(searchField);
        panel.add(searchButton);
        panel.add(allButton);
        panel.add(expiringButton);
        panel.add(lowStockButton);

        return panel;
    }

    private void addItem() {
        AddItemWindow window = new AddItemWindow(currentUserId);
        window.setVisible(true);

        Object[] newItem = window.getCreatedRow();
        if (newItem != null) {
            controller.addItem(newItem);
            tableModel.addRow(newItem);
            updateReport();
        }
    }

    private void deleteItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();
        if (controller.deleteItem(id)) {
            tableModel.removeRow(selectedRow);
            updateReport();
        }
    }

    private Object[] showEditWindow(Object[] original) {
        EditItemWindow window = new EditItemWindow(original[1].toString(), original[2].toString(),
                Integer.parseInt(original[3].toString()), original[4].toString(),
                Integer.parseInt(original[5].toString()), original[6].toString());

        window.setVisible(true);
        return window.getEditedRow();
    }

    private void editItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select item to edit.");
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();
        Object[] original = findItemById(id);
        if (original == null)
            return;

        Object[] updated = showEditWindow(original);
        if (updated != null) {
            // Apply changes to data model
            for (int i = 1; i < 7; i++) {
                original[i] = updated[i - 1];
                tableModel.setValueAt(original[i], selectedRow, i);
            }
            itemManager.saveItems(allItems);
            updateReport();
        }
    }

    // Consume / Restock

    private void consumeItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an item to consume.");
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();

        String input = JOptionPane.showInputDialog(this, "Consume amount:");
        if (input == null)
            return; // user pressed cancel

        int amount;

        try {
            amount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            return;
        }

        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be greater than 0.");
            return;
        }

        int currentQty = (int) tableModel.getValueAt(selectedRow, 3);
        if (amount > currentQty) {
            JOptionPane.showMessageDialog(this,
                    "You cannot consume more than the available quantity (" + currentQty + ").");
            return;
        }

        Integer newQty = controller.consumeItem(id, amount);
        tableModel.setValueAt(newQty, selectedRow, 3);
        updateReport();
    }

    private void restockItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Select an item to restock.");
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();

        String input = JOptionPane.showInputDialog(this, "Restock amount:");
        if (input == null)
            return; // cancel

        int amount;

        try {
            amount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            return;
        }

        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be greater than 0.");
            return;
        }

        Integer newQty = controller.restockItem(id, amount);
        tableModel.setValueAt(newQty, selectedRow, 3);
        updateReport();
    }

    // Data Loading and Filtering

    private void loadItems() {
        ArrayList<Object[]> loaded = itemManager.loadItems();

        allItems.clear();
        allItems.addAll(loaded);

        currentView = new ArrayList<>(allItems);

        tableModel.setRowCount(0);
        for (Object[] row : allItems)
            tableModel.addRow(row);

        updateReport();
    }

    private void filterTable() {
        String query = searchField.getText();
        setTableData(ItemFilters.filterBySearch(allItems, query));
        headerLabel.setText("Search: " + query);
    }

    private void filterLowStock() {
        setTableData(ItemFilters.filterLowStock(allItems));
        headerLabel.setText("Low Stock Items");
    }

    private void filterExpiring() {
        setTableData(ItemFilters.filterExpiring(allItems, 15));
        headerLabel.setText("Expiring Soon (<15 Days)");
    }

    private void showAll() {
        setTableData(new ArrayList<>(allItems));
        headerLabel.setText("All Items");
    }

    private void openShoppingList() {
        // Gather items that are low in stock for the shopping list
        ArrayList<Object[]> lowStockItems = new ArrayList<>();
        for (Object[] item : allItems) {
            int quantity = (int) item[3];
            int threshold = (int) item[5];
            if (quantity <= threshold)
                lowStockItems.add(item);
        }
        new ShoppingListWindow(lowStockItems, currentUserId, this);
    }

    private void setTableData(ArrayList<Object[]> list) {
        currentView = list;
        tableModel.setRowCount(0);
        for (Object[] row : list) {
            tableModel.addRow(row);
        }
    }

    private Object[] findItemById(String id) {
        for (Object[] item : allItems) {
            if (item[0].toString().equals(id)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Updates the summary report at the bottom of the dashboard. Counts total
     * items, low stock items, and items expiring within 15 days.
     */
    private void updateReport() {
        int total = allItems.size();
        int lowStock = 0;
        int expiringSoon = 0;

        for (Object[] item : allItems) {
            int quantity = (int) item[3];
            int threshold = (int) item[5];
            if (quantity <= threshold)
                lowStock++;

            String expiry = item[6].toString();
            if (!expiry.equals("-")) {
                try {
                    long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(expiry));
                    if (daysLeft >= 0 && daysLeft <= 15)
                        expiringSoon++;
                } catch (Exception ignored) {
                }
            }
        }

        reportLabel.setText("Items: " + total + " | Low Stock: " + lowStock + " | Expiring Soon: " + expiringSoon);
    }
}
