import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ItemManager {

    private final String userId;
    private final File file = new File("items.txt");

    public ItemManager(String userId) {
        this.userId = userId;
    }

    // Load items for this user only
    public ArrayList<Object[]> loadItems() {
        ArrayList<Object[]> list = new ArrayList<>();

        try (Scanner reader = new Scanner(file)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] p = line.split(" \\| ");
                if (p.length < 8) continue;

                if (!p[0].equals(userId)) continue;

                String expiry = p[7].equals("-") ? "" : p[7];

                Object[] row = {
                        p[1],                 // ID
                        p[2],                 // Name
                        p[3],                 // Category
                        Integer.parseInt(p[4]), // Quantity
                        p[5],                 // Unit
                        Integer.parseInt(p[6]), // Threshold
                        expiry
                };

                list.add(row);
            }
        } catch (IOException ignored) {}

        return list;
    }


    // Save all items for this user
    public void saveItems(ArrayList<Object[]> items) {
        ArrayList<String> otherUsersLines = readOtherUsers();

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {

            // Rewrite other users
            for (String line : otherUsersLines)
                writer.println(line);

            // Write current user’s items
            for (Object[] row : items) {
                String expiry = (row[6] == null || row[6].toString().isEmpty())
                        ? "-" : row[6].toString();

                writer.println(
                        userId + " | " + row[0] + " | " + row[1] + " | " +
                                row[2] + " | " + row[3] + " | " + row[4] + " | " +
                                row[5] + " | " + expiry
                );
            }

        } catch (IOException ignored) {}
    }


    // Read all lines that belong to OTHER users
    private ArrayList<String> readOtherUsers() {
        ArrayList<String> list = new ArrayList<>();

        if (!file.exists()) return list;

        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] p = line.split(" \\| ");
                if (p.length < 2) continue;

                if (!p[0].equals(userId))
                    list.add(line);
            }
        } catch (Exception ignored) {}

        return list;
    }
}
