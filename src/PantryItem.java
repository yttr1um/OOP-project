import java.util.UUID;
import java.io.*;

public class PantryItem implements Identifiable {
    private String userId;
    private String id;
    private String name;
    private String category;
    private int quantity;
    private String unit;
    private int threshold;
    private String expiryDate;

    public PantryItem(String userId, String name, String category, int quantity,
                      String unit, int threshold, String expiryDate) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.threshold = threshold;
        this.expiryDate = expiryDate;

        try {
            // Save pantry item to items.txt (database)
            FileWriter outFile = new FileWriter("items.txt", true);
            outFile.write(String.valueOf(this));
            outFile.close();
        } catch (IOException _) { }
    }

    public String getUserId() {
        return userId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + category + " | " + quantity + " | " + unit + " | "
                + threshold + " | " + expiryDate + "\n";
    }

}
