package client.gui;

import client.core.ChatClient;
import client.gui.component.ChatBubble;
import shared.domain.FileInfo;
import shared.domain.User;
import shared.dto.FileResponse;
import shared.dto.MessageResponse;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;

/**
 * Main chat window UI.
 * Handles text messages, file sending/receiving, and user interface events.
 */
public class ChatWindow extends JFrame {

    private final User user;
    private final String roomId;
    private final JTextField inputField;
    private final ChatClient client;
    private final JPanel chatPanel;
    private final JScrollPane chatScrollPane;
    private File selectedFile = null;

    public ChatWindow(User user, String roomId) {
        this.user = user;
        this.roomId = roomId;

        setTitle("Chat - " + roomId);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.WHITE);

        chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(chatScrollPane, BorderLayout.CENTER);

        inputField = new JTextField();
        JButton sendButton = new JButton("Send");
        JButton fileButton = new JButton("Select File");

        JButton exitButton = new JButton("Exit");
        exitButton.addActionListener(e -> handleExit());

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.add(exitButton, BorderLayout.WEST);
        buttonPanel.add(fileButton, BorderLayout.CENTER);
        buttonPanel.add(sendButton, BorderLayout.EAST);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(inputField, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new SendHandler());
        inputField.addActionListener(new SendHandler());
        fileButton.addActionListener(e -> handleFileSelection());

        client = new ChatClient("localhost", 12345, user, roomId, this::appendTextMessage, this::handleReceivedFile);

        setVisible(true);
    }

    private void handleFileSelection() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a file to send");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            inputField.setEnabled(false);
        }
    }

    private void appendTextMessage(MessageResponse response) {
        boolean isMine = !response.isSystemMessage() && response.getSenderId().equals(user.getUserId());
        ChatBubble bubble = new ChatBubble(response, isMine);

        chatPanel.add(bubble);
        chatPanel.revalidate();
        chatPanel.repaint();
        scrollToBottom();
    }

    public void handleReceivedFile(FileResponse response) {
        boolean isMine = response.getSenderId().equals(user.getUserId());
        String username = isMine ? "" : (response.getSender() + " : ");
        FileInfo fileInfo = response.getFileInfo();

        JPanel wrapper = new JPanel(new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBackground(isMine ? new Color(220, 248, 198) : new Color(240, 240, 240));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        if (!isMine) {
            JLabel nameLabel = new JLabel(username);
            nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            nameLabel.setForeground(Color.GRAY);
            contentPanel.add(nameLabel, BorderLayout.NORTH);
        }

        if (fileInfo.isImage()) {
            ImageIcon icon = new ImageIcon(fileInfo.getData());
            Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(scaled));
            imgLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            imgLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    JLabel bigImage = new JLabel(icon);
                    JScrollPane scroll = new JScrollPane(bigImage);
                    scroll.setPreferredSize(new Dimension(icon.getIconWidth(),icon.getIconHeight()));

                    Object[] options = {"OK", "Save"};
                    int result = JOptionPane.showOptionDialog(ChatWindow.this, scroll, "Image Preview: " + fileInfo.getFileName(), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

                    if (result == 1) { // "Save" clicked
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setSelectedFile(new java.io.File(fileInfo.getFileName()));
                        int userSelection = fileChooser.showSaveDialog(ChatWindow.this);
                        if (userSelection == JFileChooser.APPROVE_OPTION) {
                            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(fileChooser.getSelectedFile())) {
                                fos.write(fileInfo.getData());
                                JOptionPane.showMessageDialog(ChatWindow.this, "File saved successfully.");
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(ChatWindow.this, "Failed to save the file.");
                            }
                        }
                    }
                }
            });
            contentPanel.add(imgLabel, BorderLayout.CENTER);
        } else {
            JLabel fileLabel = new JLabel("File: " + fileInfo.getFileName());
            JButton saveBtn = getSaveBtn(fileInfo);

            JPanel fileRow = new JPanel(new BorderLayout());
            fileRow.setOpaque(false);
            fileRow.add(fileLabel, BorderLayout.CENTER);
            fileRow.add(saveBtn, BorderLayout.EAST);

            contentPanel.add(fileRow, BorderLayout.CENTER);
        }

        wrapper.add(contentPanel);
        chatPanel.add(wrapper);

        chatPanel.revalidate();
        chatPanel.repaint();
        scrollToBottom();
    }

    private JButton getSaveBtn(FileInfo fileInfo) {
        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(fileInfo.getFileName()));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.write(chooser.getSelectedFile().toPath(), fileInfo.getData());
                    JOptionPane.showMessageDialog(this, "File saved successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to save file: " + ex.getMessage());
                }
            }
        });
        return saveBtn;
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            chatPanel.revalidate();
            chatScrollPane.getVerticalScrollBar().setValue(chatScrollPane.getVerticalScrollBar().getMaximum());
        });
    }

    private class SendHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedFile != null) {
                new Thread(() -> {
                    try {
                        byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                        boolean isImage = selectedFile.getName().matches("(?i).+\\.(png|jpg|jpeg|gif)$");
                        FileInfo fileInfo = new FileInfo(selectedFile.getName(), fileData, isImage);
                        client.getFileSender().sendFile(fileInfo, user, roomId);
                        SwingUtilities.invokeLater(() -> {
                            selectedFile = null;
                            inputField.setEnabled(true);
                            inputField.setText("");
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ChatWindow.this, "Failed to send file: " + ex.getMessage()));
                    }
                }).start();
            } else {
                String message = inputField.getText().trim();
                if (!message.isEmpty()) {
                    try {
                        client.getSender().sendText(message, user, roomId);
                        inputField.setText("");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ChatWindow.this, "Failed to send message: " + ex.getMessage());
                    }
                }
            }
        }
    }

    private void handleExit() {
        int choice = JOptionPane.showConfirmDialog(this, "Do you want to leave this chat room?", "Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                client.getSender().sendQuit(user, roomId);
            } catch (Exception ex) {
                System.err.println("Failed to send quit message: " + ex.getMessage());
            }
            client.disconnect();

            SwingUtilities.invokeLater(() -> new RoomSelectionWindow(user));
            dispose();
        }
    }
}
