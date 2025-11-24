import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

public class EditItemWindow extends JDialog {

    private JTextField nameInput, categoryInput, quantityInput, unitInput, thresholdInput, expiryDateInput;
    private JButton saveBtn;
    private Object[] editedRow = null;

    public Object[] getEditedRow() {
        return editedRow;
    }

    public EditItemWindow(String name, String category, int quantity, String unit, int threshold, String expiryDate) {

        setModal(true);
        setTitle("Edit Item");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(9, 2, 0, 5));

        add(new JLabel("Edit Item"));
        add(new JLabel(""));

        nameInput = new JTextField(name);
        categoryInput = new JTextField(category);
        quantityInput = new JTextField(String.valueOf(quantity));
        unitInput = new JTextField(unit);
        thresholdInput = new JTextField(String.valueOf(threshold));
        expiryDateInput = new JTextField(expiryDate);

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

        DocumentListener listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateButtonState(); }
            public void removeUpdate(DocumentEvent e) { updateButtonState(); }
            public void changedUpdate(DocumentEvent e) { updateButtonState(); }
        };

        nameInput.getDocument().addDocumentListener(listener);
        categoryInput.getDocument().addDocumentListener(listener);
        quantityInput.getDocument().addDocumentListener(listener);
        unitInput.getDocument().addDocumentListener(listener);
        thresholdInput.getDocument().addDocumentListener(listener);
        expiryDateInput.getDocument().addDocumentListener(listener);

        add(new JLabel(""));
        add(new JLabel(""));

        saveBtn = new JButton("Save");
        saveBtn.setEnabled(true);

        saveBtn.addActionListener(e -> {
            String error = validateInputs();
            if (error != null) {
                JOptionPane.showMessageDialog(this, error, "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String newExpiryDate = expiryDateInput.getText();
            if (newExpiryDate.trim().isEmpty()) {
                newExpiryDate = "-";
            }

            editedRow = new Object[] {
                    nameInput.getText(),
                    categoryInput.getText(),
                    Integer.parseInt(quantityInput.getText()),
                    unitInput.getText(),
                    Integer.parseInt(thresholdInput.getText()),
                    newExpiryDate
            };

            dispose();
        });

        add(new JLabel(""));
        add(saveBtn);
    }

    /** Enable button only if required fields are filled */
    private void updateButtonState() {
        boolean ok = !nameInput.getText().trim().isEmpty()
                && !categoryInput.getText().trim().isEmpty()
                && !quantityInput.getText().trim().isEmpty()
                && !unitInput.getText().trim().isEmpty()
                && !thresholdInput.getText().trim().isEmpty();
        saveBtn.setEnabled(ok);
    }

    private String validateInputs() {

        // Quantity integer
        try {
            int quantity = Integer.parseInt(quantityInput.getText());
            if (quantity < 0)
                return "Quantity must be a positive number.";
        } catch (NumberFormatException e) {
            return "Quantity must be a whole number.";
        }

        // Threshold integer
        try {
            int threshold = Integer.parseInt(thresholdInput.getText());
            if (threshold < 0)
                return "Threshold must be a positive number.";
        } catch (NumberFormatException e) {
            return "Threshold must be a whole number.";
        }

        // Date validation
        String date = expiryDateInput.getText().trim();
        if (!date.isEmpty() && !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return "Expiry date must follow the format YYYY-MM-DD.";
        }

        return null; // all good
    }
}
