import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class ItemFilters {

    public static ArrayList<Object[]> filterBySearch(ArrayList<Object[]> items, String text) {
        text = text.toLowerCase();
        ArrayList<Object[]> result = new ArrayList<>();
        for (Object[] row : items) {
            if (row[1].toString().toLowerCase().contains(text) || row[2].toString().toLowerCase().contains(text)) {
                result.add(row);
            }
        }
        return result;
    }

    public static ArrayList<Object[]> filterLowStock(ArrayList<Object[]> items) {
        ArrayList<Object[]> result = new ArrayList<>();
        for (Object[] row : items) {
            int qty = (int) row[3];
            int threshold = (int) row[5];
            if (qty <= threshold)
                result.add(row);
        }
        return result;
    }

    public static ArrayList<Object[]> filterExpiring(ArrayList<Object[]> items, int days) {
        ArrayList<Object[]> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Object[] row : items) {
            String expStr = row[6].toString();
            if (expStr.isEmpty() || expStr.equals("-"))
                continue;
            try {
                LocalDate exp = LocalDate.parse(expStr);
                long diff = ChronoUnit.DAYS.between(today, exp);
                if (diff >= 0 && diff <= days)
                    result.add(row);
            } catch (Exception ignored) {
            }
        }
        return result;
    }
}
