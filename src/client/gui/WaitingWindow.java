package client.gui;

import shared.domain.User;
import shared.dto.ClientRequest;
import shared.dto.MessageResponse;

import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Waiting window shown during random chat matching.
 */
public class WaitingWindow extends JFrame {

    private final User user;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public WaitingWindow(User user) {
        this.user = user;

        setTitle("Random Chat - Waiting for Match");
        setSize(300, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel messageLabel = new JLabel("Waiting for another user...", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        add(messageLabel, BorderLayout.CENTER);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> cancelWaiting());
        add(cancelButton, BorderLayout.SOUTH);

        setVisible(true);
        startMatching(); // Start attempting to match
    }

    /**
     * Starts the process of waiting and listening for a match.
     */
    private void startMatching() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 12345);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                ClientRequest req = new ClientRequest("start_random", "", "", user, null);
                out.writeObject(req);
                out.flush();

                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof MessageResponse response) {
                        if ("matched".equals(response.getMessage())) {
                            String roomId = response.getRoomId();
                            openChatRoom(roomId);
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Connection to server failed.")
                );
                dispose();
            }
        }).start();
    }

    /**
     * Cancels the waiting state and returns to the room selection window.
     */
    private void cancelWaiting() {
        try {
            ClientRequest request = new ClientRequest("cancel_waiting", "", "", user, null);
            out.writeObject(request);
            out.flush();
        } catch (Exception ignored) {
        } finally {
            dispose();
            new RoomSelectionWindow(user);
        }
    }

    /**
     * Opens the chat window once a match has been made.
     */
    private void openChatRoom(String roomId) {
        SwingUtilities.invokeLater(() -> {
            dispose();
            new ChatWindow(user, roomId);
        });
    }
}
