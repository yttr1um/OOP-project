import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

class ExpiryRowRenderer extends DefaultTableCellRenderer {

    private DefaultTableModel model;

    public ExpiryRowRenderer(DefaultTableModel model) {
        this.model = model;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int col) {

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
        } catch (Exception ignored) {
        }

        // Highlight expiring soon
        if (!expiry.equals("-") && !expiry.isEmpty()) {
            try {
                java.time.LocalDate expDate = java.time.LocalDate.parse(expiry);
                long days = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), expDate);

                if (days >= 0 && days <= 15) {
                    if (!isSelected)
                        component.setBackground(new Color(255, 180, 180)); // light orange
                }

            } catch (Exception ignored) {
            }
        }

        return component;
    }
}
