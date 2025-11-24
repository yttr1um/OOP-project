import java.io.*;
import java.util.ArrayList;

public class ItemManager {
    private final String userId;
    private static final String FILE_PATH = "items.txt";

    public ItemManager(String userId) {
        this.userId = userId;
    }

    /**
     * Load items for the current user only.
     */
    public ArrayList<Object[]> loadItems() {
        ArrayList<Object[]> userItems = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] parts = line.split("\\|");
                if (parts[0].trim().equals(userId)) {
                    Object[] row = new Object[7];
                    for (int i = 1; i <= 7; i++) {
                        row[i - 1] = parseValue(parts[i].trim());
                    }
                    userItems.add(row);
                }
            }
        } catch (IOException _) {}
        return userItems;
    }

    /**
     * Save items for the current user only. Other users remain intact.
     */
    public void saveItems(ArrayList<Object[]> currentUserItems) {
        ArrayList<String> allLines = new ArrayList<>();
        // Load all existing lines
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] parts = line.split("\\|");
                // Keep items of other users
                if (!parts[0].trim().equals(userId)) {
                    allLines.add(line);
                }
            }
        } catch (IOException ignored) {
        }

        // Add/update current user's items
        for (Object[] row : currentUserItems) {
            StringBuilder sb = new StringBuilder();
            sb.append(userId).append(" | ");
            for (int i = 0; i < 7; i++) {
                sb.append(row[i].toString()).append(" | ");
            }
            sb.setLength(sb.length() - 3); // remove last " | "
            allLines.add(sb.toString());
        }

        // Write back all users' items
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (String l : allLines) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException _) {}
    }

    private Object parseValue(String s) {
        // Try parsing numbers, else return string
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return s;
        }
    }
}
