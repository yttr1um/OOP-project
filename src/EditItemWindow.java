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
        add(new JLabel("Name:"));
        add(nameInput);

        categoryInput = new JTextField(category);
        add(new JLabel("Category:"));
        add(categoryInput);

        quantityInput = new JTextField(String.valueOf(quantity));
        add(new JLabel("Quantity:"));
        add(quantityInput);

        unitInput = new JTextField(unit);
        add(new JLabel("Unit:"));
        add(unitInput);

        thresholdInput = new JTextField(String.valueOf(threshold));
        add(new JLabel("Threshold:"));
        add(thresholdInput);

        expiryDateInput = new JTextField(expiryDate);
        add(new JLabel("Expiry Date (YYYY-MM-DD):"));
        add(expiryDateInput);

        DocumentListener listener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                check();
            }

            public void removeUpdate(DocumentEvent e) {
                check();
            }

            public void changedUpdate(DocumentEvent e) {
                check();
            }
        };

        nameInput.getDocument().addDocumentListener(listener);
        categoryInput.getDocument().addDocumentListener(listener);
        quantityInput.getDocument().addDocumentListener(listener);
        unitInput.getDocument().addDocumentListener(listener);
        thresholdInput.getDocument().addDocumentListener(listener);

        add(new JLabel(""));
        add(new JLabel(""));

        saveBtn = new JButton("Save");
        saveBtn.setEnabled(true);

        saveBtn.addActionListener(e -> {
            editedRow = new Object[] { nameInput.getText(), categoryInput.getText(),
                    Integer.parseInt(quantityInput.getText()), unitInput.getText(),
                    Integer.parseInt(thresholdInput.getText()), expiryDateInput.getText() };
            dispose();
        });

        add(new JLabel(""));
        add(saveBtn);
    }

    private void check() {
        boolean ok = !nameInput.getText().trim().isEmpty() && !categoryInput.getText().trim().isEmpty()
                && !quantityInput.getText().trim().isEmpty() && !unitInput.getText().trim().isEmpty()
                && !thresholdInput.getText().trim().isEmpty();

        saveBtn.setEnabled(ok);
    }
}
