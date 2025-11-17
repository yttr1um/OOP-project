import java.util.UUID;
import java.io.*;

public class User implements Identifiable{
    private String name;
    private String email;
    private String phone;
    private String password;
    private String id;

    public User(String name, String email, String phone, String password) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.id = UUID.randomUUID().toString(); // Generate a random ID for the user

        // Store User data in users.txt
        try {
            FileWriter outFile = new FileWriter("users.txt", true);
            outFile.write(String.valueOf(this)); // will use the overridden toString method

            outFile.close();


        } catch (IOException _) { }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s | %s%n", id, name, phone, email, password);
    }
}