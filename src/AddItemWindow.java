import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

public class AddItemWindow extends JDialog {

    private final JTextField nameInput, categoryInput, quantityInput, unitInput, thresholdInput, expiryDateInput;
    private final JButton addItemBtn;
    private Object[] createdRow = null; // Row data to return to main table
    private final String userId;

    public AddItemWindow(String userId) {
        this.userId = userId;

        setTitle("Add Item");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(9, 2, 0, 5));
        setModal(true);

        add(new JLabel("Add Item")); // Header
        add(new JLabel(""));

        nameInput = new JTextField();
        categoryInput = new JTextField();
        quantityInput = new JTextField();
        unitInput = new JTextField();
        thresholdInput = new JTextField();
        expiryDateInput = new JTextField();

        add(new JLabel("Name:"));
        add(nameInput);
        add(new JLabel("Category:"));
        add(categoryInput);
        add(new JLabel("Quantity:"));
        add(quantityInput);
        add(new JLabel("Unit:"));
        add(unitInput);
        add(new JLabel("Threshold:"));
        add(thresholdInput);
        add(new JLabel("Expiry Date (YYYY-MM-DD):"));
        add(expiryDateInput);

        // Enable Add button only when required fields are filled
        DocumentListener fieldListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateButtonState();
            }
        };

        nameInput.getDocument().addDocumentListener(fieldListener);
        categoryInput.getDocument().addDocumentListener(fieldListener);
        quantityInput.getDocument().addDocumentListener(fieldListener);
        unitInput.getDocument().addDocumentListener(fieldListener);
        thresholdInput.getDocument().addDocumentListener(fieldListener);

        add(new JLabel(""));
        add(new JLabel(""));

        addItemBtn = new JButton("Add Item");
        addItemBtn.setFocusable(false);
        addItemBtn.setEnabled(false);
        addItemBtn.addActionListener(e -> {
            // Validate inputs first
            String error = validateInputs();
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PantryItem item = createItem();
            createdRow = new Object[] { item.getId(), item.getName(), item.getCategory(), item.getQuantity(),
                    item.getUnit(), item.getThreshold(), item.getExpiryDate() };

            dispose(); // Close dialog only if valid
        });

        add(new JLabel(""));
        add(addItemBtn);
    }

    // Create PantryItem object from input fields
    private PantryItem createItem() {
        String name = nameInput.getText();
        String category = categoryInput.getText();
        int quantity = Integer.parseInt(quantityInput.getText());
        String unit = unitInput.getText();
        int threshold = Integer.parseInt(thresholdInput.getText());
        String expiryDate = expiryDateInput.getText();
        if (expiryDate.trim().isEmpty()) {
            expiryDate = "-";
        }

        return new PantryItem(userId, name, category, quantity, unit, threshold, expiryDate);
    }

    // Return created row for main table
    public Object[] getCreatedRow() {
        return createdRow;
    }

    // Enable Add button only if all required fields are non-empty
    private void updateButtonState() {
        boolean enabled = !nameInput.getText().trim().isEmpty() && !categoryInput.getText().trim().isEmpty()
                && !quantityInput.getText().trim().isEmpty() && !unitInput.getText().trim().isEmpty()
                && !thresholdInput.getText().trim().isEmpty();
        addItemBtn.setEnabled(enabled);
    }

    private String validateInputs() {
        // Check quantity integer
        try {
            int quantity = Integer.parseInt(quantityInput.getText());
            if (quantity < 0)
                return "Quantity must be a positive number.";
        } catch (NumberFormatException e) {
            return "Quantity must be a whole number.";
        }

        // Check threshold integer
        try {
            int threshold = Integer.parseInt(thresholdInput.getText());
            if (threshold < 0)
                return "Threshold must be a positive number.";
        } catch (NumberFormatException e) {
            return "Threshold must be a whole number.";
        }

        // Validate date format only if not empty
        String date = expiryDateInput.getText().trim();
        if (!date.isEmpty()) {
            if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return "Expiry date must follow the format YYYY-MM-DD.";
            }
        }

        return null; // All good
    }

}
