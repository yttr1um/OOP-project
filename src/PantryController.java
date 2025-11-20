import java.util.ArrayList;

public class PantryController {

    private final ItemManager itemManager; // Handles saving/loading items
    private final ArrayList<Object[]> allItems; // Stores all pantry items

    public PantryController(ItemManager itemManager, ArrayList<Object[]> allItems) {
        this.itemManager = itemManager;
        this.allItems = allItems;
    }

    // Add a new item and save to file
    public Object[] addItem(Object[] newRow) {
        allItems.add(newRow);
        itemManager.saveItems(allItems);
        return newRow;
    }

    // Delete an item by ID and save changes
    public boolean deleteItem(String id) {
        boolean removed = allItems.removeIf(row -> row[0].toString().equals(id));
        if (removed) {
            itemManager.saveItems(allItems);
        }
        return removed;
    }

    // Edit an existing item identified by ID and save changes
    public Object[] editItem(String id, Object[] updated) {
        Object[] row = findItemById(id);
        if (row == null) {
            return null; // Return null if item not found
        }

        row[1] = updated[0];
        row[2] = updated[1];
        row[3] = updated[2];
        row[4] = updated[3];
        row[5] = updated[4];
        row[6] = updated[5];

        itemManager.saveItems(allItems);
        return row;
    }

    // Consume a specified amount of an item, returns new quantity or null
    public Integer consumeItem(String id, int amount) {
        Object[] row = findItemById(id);
        if (row == null) {
            return null; // Item not found
        }

        int qty = (int) row[3];
        if (amount <= 0 || amount > qty) {
            return null; // Invalid consume amount
        }

        qty -= amount;
        row[3] = qty;

        itemManager.saveItems(allItems);
        return qty;
    }

    // Restock a specified amount of an item, returns new quantity or null
    public Integer restockItem(String id, int amount) {
        Object[] row = findItemById(id);
        if (row == null) {
            return null; // Item not found
        }

        if (amount <= 0) {
            return null; // Invalid restock amount
        }

        int qty = (int) row[3] + amount;
        row[3] = qty;

        itemManager.saveItems(allItems);
        return qty;
    }

    // Find an item in allItems by its ID
    private Object[] findItemById(String id) {
        for (Object[] row : allItems) {
            if (row[0].toString().equals(id)) {
                return row; // Return item if ID matches
            }
        }
        return null; // Return null if not found
    }
}
