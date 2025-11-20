import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;

public class LoginWindow extends JFrame {

    private final JTextField nameInput;
    private final JPasswordField passwordInput;
    private final JButton loginBtn;
    private final JButton registerBtn;

    public LoginWindow() {
        setTitle("Login");
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 150);
        setLayout(new GridLayout(3, 2, 0, 5));

        nameInput = new JTextField();
        add(new JLabel("Name:")); add(nameInput);

        passwordInput = new JPasswordField();
        add(new JLabel("Password:")); add(passwordInput);

        // Enable login button only when fields are non-empty
        DocumentListener fieldListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateButtonState(); }
            @Override public void removeUpdate(DocumentEvent e) { updateButtonState(); }
            @Override public void changedUpdate(DocumentEvent e) { updateButtonState(); }
        };
        nameInput.getDocument().addDocumentListener(fieldListener);
        passwordInput.getDocument().addDocumentListener(fieldListener);

        loginBtn = new JButton("Login");
        loginBtn.setEnabled(false); // Disabled until fields filled
        registerBtn = new JButton("Register");

        add(loginBtn); add(registerBtn);

        loginBtn.addActionListener(e -> {
            String name = nameInput.getText();
            String password = new String(passwordInput.getPassword());
            checkUser(name, password); // Verify login credentials
        });

        registerBtn.addActionListener(e -> {
            dispose();
            new RegisterDialog(); // Open registration window
        });

        setVisible(true);
    }

    // Check username/password against users.txt
    private void checkUser(String name, String password) {
        try (Scanner reader = new Scanner(new FileReader("users.txt"))) {

            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] userInfo = line.split("\\|");
                if (userInfo.length < 5) continue; // Skip invalid lines

                String userId = userInfo[0].trim();
                String userName = userInfo[1].trim();
                String userPassword = userInfo[4].trim();

                if (userName.equals(name) && userPassword.equals(password)) {
                    dispose();
                    new PantryDashboard(name, userId); // Open dashboard
                    return;
                }
            }

            JOptionPane.showMessageDialog(this, "Invalid username or password."); // Login failed

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading users.txt"); // File error
        }
    }

    // Enable login button if both fields are non-empty
    private void updateButtonState() {
        boolean enabled = !nameInput.getText().trim().isEmpty() && passwordInput.getPassword().length > 0;
        loginBtn.setEnabled(enabled);
    }

    public static void main(String[] args) {
        new LoginWindow(); // Launch login window
    }
}
