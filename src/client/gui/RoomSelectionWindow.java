package client.gui;

import client.handler.RoomListFetcher;
import shared.domain.User;
import shared.dto.RoomListRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;

/**
 * Room selection window.
 * Allows the user to create or join a chat room or start random chat.
 */
public class RoomSelectionWindow extends JFrame {

    private final User user;
    private final JPanel roomListPanel;

    public RoomSelectionWindow(User user) {
        this.user = user;

        setTitle("Select a chat room to join - " + user.getUsername());
        setSize(480, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        roomListPanel = new JPanel();
        roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(roomListPanel);

        JPanel buttonPanel = new JPanel();
        JButton createButton = new JButton("Create Room");
        JButton randomButton = new JButton("Random Chat");
        JButton refreshButton = new JButton("Refresh");

        buttonPanel.add(createButton);
        buttonPanel.add(randomButton);
        buttonPanel.add(refreshButton);

        add(new JLabel("Available chat rooms:"), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        createButton.addActionListener(e -> {
            String roomId = JOptionPane.showInputDialog(this, "Enter new room name:");
            if (roomId != null && !roomId.trim().isEmpty()) openChatWindow(roomId.trim());
        });

        randomButton.addActionListener(e -> {
            new WaitingWindow(user); // Open waiting window for random match
            dispose();               // Close current window
        });

        refreshButton.addActionListener(e -> fetchRoomList());

        fetchRoomList();
        setVisible(true);
    }

    /**
     * Fetch the room list from the server asynchronously.
     */
    private void fetchRoomList() {
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 12347);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                out.writeObject(new RoomListRequest());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                RoomListFetcher fetcher = new RoomListFetcher(socket, in,
                        roomMap -> SwingUtilities.invokeLater(() -> updateRoomPanels(roomMap)));
                fetcher.start();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(this, "Failed to load room list."));
            }
        }).start();
    }

    /**
     * Update the room list UI with new data.
     */
    private void updateRoomPanels(Map<String, Integer> rooms) {
        roomListPanel.removeAll();
        for (Map.Entry<String, Integer> entry : rooms.entrySet()) {
            JPanel panel = createRoomPanel(entry.getKey(), entry.getValue());
            roomListPanel.add(panel);
            roomListPanel.add(Box.createVerticalStrut(10));
        }
        roomListPanel.revalidate();
        roomListPanel.repaint();
    }

    /**
     * Creates a panel for a single room entry.
     *
     * @param roomId Room name
     * @param participantCount Number of current participants
     * @return JPanel representing the room
     */
    private JPanel createRoomPanel(String roomId, int participantCount) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(440, 60));
        panel.setMaximumSize(new Dimension(440, 60));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(roomId + "  (Users: " + participantCount + ")");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JButton enterButton = new JButton("Join");
        enterButton.addActionListener(e -> openChatWindow(roomId));

        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(enterButton, BorderLayout.EAST);

        panel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(new Color(245, 245, 255));
            }

            public void mouseExited(MouseEvent e) {
                panel.setBackground(Color.WHITE);
            }
        });

        return panel;
    }

    /**
     * Opens a new chat window for the selected room.
     */
    private void openChatWindow(String roomId) {
        new ChatWindow(user, roomId);
        dispose();
    }
}
