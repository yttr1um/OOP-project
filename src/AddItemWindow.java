import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

public class AddItemWindow extends JDialog {

    private JTextField nameInput, categoryInput, quantityInput ,unitInput, thresholdInput, expiryDateInput;
    private JButton addItemBtn;
    private Object[] createdRow = null;

    private String userId;

    public AddItemWindow(String userId) {

        this.userId = userId;

        setTitle("Add Item");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(9, 2, 0, 5));
        setModal(true);

        add(new JLabel("Add Item"));
        add(new JLabel(""));

        nameInput = new JTextField();
        add(new JLabel("Name:"));
        add(nameInput);

        categoryInput = new JTextField();
        add(new JLabel("Category:"));
        add(categoryInput);

        quantityInput = new JTextField();
        add(new JLabel("Quantity:"));
        add(quantityInput);

        unitInput = new JTextField();
        add(new JLabel("Unit:"));
        add(unitInput);

        thresholdInput = new JTextField();
        add(new JLabel("Threshold:"));
        add(thresholdInput);

        expiryDateInput = new JTextField();
        add(new JLabel("Expiry Date (YYYY-MM-DD):"));
        add(expiryDateInput);

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

        // Enables the button once the fields have been filled
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
            PantryItem item = createItem();

            createdRow = new Object[]{
                    item.getId(), item.getName(), item.getCategory(),
                    item.getQuantity(), item.getUnit(),
                    item.getThreshold(), item.getExpiryDate()
            };

            dispose();
        });

        add(new JLabel(""));
        add(addItemBtn);

    }

    private PantryItem createItem() {
        String name = nameInput.getText();
        String category = categoryInput.getText();
        int quantity = Integer.parseInt(quantityInput.getText());
        String unit = unitInput.getText();
        int threshold = Integer.parseInt(thresholdInput.getText());
        String expiryDate = expiryDateInput.getText();

        return new PantryItem(userId, name, category, quantity, unit, threshold, expiryDate);
    }

    public Object[] createRow() {
        PantryItem item = createItem();
        return new Object[]{
                item.getId(), item.getName(), item.getCategory(), item.getQuantity(), item.getUnit(),
                item.getThreshold(), item.getExpiryDate()
        };
    }

    public Object[] getCreatedRow() {
        return createdRow;
    }

    private void updateButtonState() {
        boolean enabled =
                !nameInput.getText().trim().isEmpty() &&
                        !categoryInput.getText().trim().isEmpty() &&
                        !quantityInput.getText().trim().isEmpty() &&
                        !unitInput.getText().trim().isEmpty() &&
                        !thresholdInput.getText().trim().isEmpty();

        addItemBtn.setEnabled(enabled);
    }
}

