package client.gui;

import shared.domain.User;

import javax.swing.*;
import java.awt.*;

/**
 * Initial landing window where users input username and display name.
 */
public class LandingWindow extends JFrame {

    public LandingWindow() {
        setTitle("Chat Application - Login");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();

        JLabel displayNameLabel = new JLabel("Display Name:");
        JTextField displayNameField = new JTextField();

        JButton enterButton = new JButton("Enter");

        add(usernameLabel);
        add(usernameField);
        add(displayNameLabel);
        add(displayNameField);
        add(enterButton);

        enterButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String displayName = displayNameField.getText().trim();

            if (username.isEmpty() || displayName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                return;
            }

            User user = new User(username, displayName);

            new RoomSelectionWindow(user); // Proceed to room selection
            dispose(); // Close current window
        });

        setVisible(true);
    }
}
