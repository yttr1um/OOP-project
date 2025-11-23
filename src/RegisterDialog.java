import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

public class RegisterDialog extends JFrame {

    private JTextField nameInput, emailInput, phoneInput;
    private JPasswordField passwordInput;
    private JButton registerBtn, backBtn;

    public RegisterDialog() {
        setTitle("Register");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null);

        setLayout(new GridLayout(5, 2, 0, 5));

        nameInput = new JTextField();
        add(new JLabel("Name:"));
        add(nameInput);

        emailInput = new JTextField();
        add(new JLabel("Email:"));
        add(emailInput);

        phoneInput = new JTextField();
        add(new JLabel("Phone:"));
        add(phoneInput);

        passwordInput = new JPasswordField();
        add(new JLabel("Password:"));
        add(passwordInput);

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
        emailInput.getDocument().addDocumentListener(fieldListener);
        phoneInput.getDocument().addDocumentListener(fieldListener);
        passwordInput.getDocument().addDocumentListener(fieldListener);

        registerBtn = new JButton("register");
        registerBtn.setEnabled(false);
        backBtn = new JButton("Back");
        add(backBtn);
        add(registerBtn);

        registerBtn.addActionListener(e -> registerUser());

        backBtn.addActionListener(e -> {
            dispose();
            new LoginWindow();
        });

        setVisible(true);
    }

    private void registerUser() {
        String name = nameInput.getText().trim();
        String email = emailInput.getText().trim();
        String phone = phoneInput.getText().trim();
        String password = new String(passwordInput.getPassword()).trim();

        // Basic empty check
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be left empty!");
            return;
        }

        // Name validation
        if (name.length() < 2) {
            JOptionPane.showMessageDialog(this, "Name must be at least 2 characters.");
            return;
        }

        // Email validation
        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return;
        }

        // Phone validation
        if (!phone.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "Phone number must contain only digits.");
            return;
        }
        if (phone.length() < 8) {
            JOptionPane.showMessageDialog(this, "Phone number must be at least 8 digits.");
            return;
        }

        // Password validation
        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.");
            return;
        }

        new User(name, email, phone, password);

        JOptionPane.showMessageDialog(this, "Registration complete.");
        dispose();
        new LoginWindow();
    }

    private void updateButtonState() {
        String name = nameInput.getText().trim();
        String email = emailInput.getText().trim();
        String phone = phoneInput.getText().trim();
        String password = new String(passwordInput.getPassword()).trim();

        boolean enabled = !name.isEmpty() && !email.isEmpty() && !phone.isEmpty() && !password.isEmpty();

        registerBtn.setEnabled(enabled);
    }
}
