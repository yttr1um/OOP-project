import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class ShoppingListWindow extends JDialog {

    private DefaultTableModel model;
    private JTable table;
    private String userId;
    private ArrayList<Object[]> entries = new ArrayList<>();
    private PantryDashboard dashboard;

    String[] cols = { "Item", "Quantity", "Unit", "Status" };

    public ShoppingListWindow(ArrayList<Object[]> lowStock, String userId, PantryDashboard dash) {

        this.userId = userId;
        this.dashboard = dash;

        setModal(true);
        setTitle("Shopping List");
        setSize(500, 350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Load existing shopping list entries from file
        loadFromFile();

        // Auto-add low stock items if not already in list
        for (Object[] row : lowStock) {
            String itemName = row[1].toString();
            int qty = (int) row[3];
            String unit = row[4].toString();

            addIfNotExists(itemName, qty, unit);
        }

        // Bottom panel buttons
        JPanel bottom = new JPanel(new FlowLayout());

        JButton addBtn = new JButton("Add");
        JButton removeBtn = new JButton("Remove");
        JButton purchaseBtn = new JButton("Mark Purchased");

        bottom.add(addBtn);
        bottom.add(removeBtn);
        bottom.add(purchaseBtn);

        add(bottom, BorderLayout.SOUTH);

        // Listeners
        addBtn.addActionListener(e -> addManual());
        removeBtn.addActionListener(e -> removeItem());
        purchaseBtn.addActionListener(e -> markPurchased());

        setVisible(true);
    }

    private void loadFromFile() {
        File file = new File("shopping_lists.txt");

        if (!file.exists())
            return;

        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine().trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] p = line.split(" ");
                if (p.length < 6) {
                    continue;
                }

                String uid = p[0];
                if (!uid.equals(userId)) {
                    continue;
                }

                // format: userId item qty unit status date
                Object[] row = { p[1], // item
                        Integer.parseInt(p[2]), p[3], // unit
                        p[4] // status
                };

                entries.add(row);
                model.addRow(row);
            }
        } catch (Exception ignored) {
        }
    }

    private void saveToFile() {
        File file = new File("shopping_lists.txt");

        ArrayList<String> keep = getStrings(file);

        // rewrite everything
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {

            for (String line : keep)
                pw.println(line);

            for (Object[] row : entries) {
                pw.println(userId + " " + row[0] + " " + row[1] + " " + row[2] + " " + row[3] + " " + LocalDate.now());
            }
        } catch (Exception ignored) {
        }
    }

    private ArrayList<String> getStrings(File file) {
        ArrayList<String> keep = new ArrayList<>();

        // keep other users' lists
        if (file.exists()) {
            try (Scanner scan = new Scanner(file)) {
                while (scan.hasNextLine()) {
                    String line = scan.nextLine().trim();
                    if (line.isEmpty())
                        continue;
                    String[] p = line.split(" ");
                    if (!p[0].equals(userId))
                        keep.add(line);
                }
            } catch (Exception ignored) {
            }
        }
        return keep;
    }

    private void addIfNotExists(String item, int qty, String unit) {
        for (Object[] row : entries) {
            if (row[0].equals(item))
                return;
        }

        Object[] row = { item, qty, unit, "Pending" };
        entries.add(row);
        model.addRow(row);
        saveToFile();
    }

    private void addManual() {
        String item = JOptionPane.showInputDialog("Item name:");
        if (item == null || item.trim().isEmpty())
            return;

        int qty;
        try {
            qty = Integer.parseInt(JOptionPane.showInputDialog("Quantity:"));
        } catch (Exception e) {
            return;
        }

        String unit = JOptionPane.showInputDialog("Unit:");
        if (unit == null || unit.trim().isEmpty())
            return;

        addIfNotExists(item, qty, unit);
    }

    private void removeItem() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;

        entries.remove(row);
        model.removeRow(row);
        saveToFile();
    }

    private void markPurchased() {
        int row = table.getSelectedRow();
        if (row < 0)
            return;

        String itemName = entries.get(row)[0].toString();
        int qty = (int) entries.get(row)[1];
        String unit = entries.get(row)[2].toString();

        // 1. Restock pantry automatically
        boolean found = false;

        for (Object[] pantryRow : dashboard.getAllItems()) {
            if (pantryRow[1].toString().equalsIgnoreCase(itemName)) {
                // item exists → increase quantity
                int currentQty = (int) pantryRow[3];
                pantryRow[3] = currentQty + qty;

                // update table display
                int pantryRowIndex = dashboard.getAllItems().indexOf(pantryRow);
                dashboard.getModel().setValueAt(currentQty + qty, pantryRowIndex, 3);

                found = true;
                break;
            }
        }

        // 2. Item doesn't exist → create new pantry item
        if (!found) {
            String newId = "I" + System.currentTimeMillis(); // simple ID
            Object[] newRow = { newId, itemName, "Misc", qty, unit, 1, "N/A" };

            dashboard.getAllItems().add(newRow);
            dashboard.getModel().addRow(newRow);
        }

        // 3. Save pantry changes to file
        dashboard.updateItemsFile();

        // 4. Remove item from shopping list
        entries.remove(row);
        model.removeRow(row);

        // 5. Save shopping list file
        saveToFile();

        JOptionPane.showMessageDialog(this, "Item purchased and added to pantry!");
    }

}