package client.gui;

import client.core.ChatClient;
import shared.domain.FileInfo;
import shared.domain.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;

public class ChatWindow extends JFrame {

    private final User user;
    private final String roomId;
    private final JTextField inputField;
    private final ChatClient client;
    private File selectedFile = null;

    private final JPanel chatPanel;
    private final JScrollPane chatScrollPane;

    public ChatWindow(User user, String roomId) {
        this.user = user;
        this.roomId = roomId;

        setTitle("Chat - " + user.getDisplayName());
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
        JButton fileButton = new JButton("파일 선택");

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(fileButton, BorderLayout.WEST);
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new SendHandler());
        inputField.addActionListener(new SendHandler());
        fileButton.addActionListener(e -> handleFileSelection());

        client = new ChatClient("localhost", 12345, user, roomId, this::appendTextMessage,  // 또는 sender를 msg에 포함하도록 포맷
                this::handleReceivedFile);


        setVisible(true);
    }

    private void handleFileSelection() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("전송할 파일을 선택하세요");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            inputField.setEnabled(false);
            appendTextMessage("\uD83D\uDCCE 파일 전송 대기 중: " + selectedFile.getName());
        }
    }

    private void appendTextMessage(String message) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JTextArea textArea = new JTextArea(message);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setOpaque(true);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        textArea.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));  // ✅ 최대 폭 제한

        if (message.startsWith("[System]")) {
            textArea.setBackground(Color.LIGHT_GRAY);
            wrapper.add(textArea, BorderLayout.CENTER);
        } else {
            int sepIdx = message.indexOf(" : ");
            String sender = sepIdx > 0 ? message.substring(0, sepIdx).trim() : "";
            boolean isMine = sender.equals(user.getDisplayName());

            if (isMine) {
                textArea.setBackground(new Color(0xDFFFD6)); // 연두
                wrapper.add(textArea, BorderLayout.EAST);
            } else {
                textArea.setBackground(Color.WHITE);
                wrapper.add(textArea, BorderLayout.WEST);
            }
        }

        chatPanel.add(wrapper);
        chatPanel.revalidate();
        chatPanel.repaint();
        scrollToBottom();
    }


    public void handleReceivedFile(FileInfo fileInfo) {
        if (fileInfo.isImage()) {
            ImageIcon icon = new ImageIcon(fileInfo.getData());
            Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            JLabel imgLabel = new JLabel(new ImageIcon(scaled));
            imgLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            imgLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            imgLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    JLabel bigImage = new JLabel(icon);
                    JScrollPane scroll = new JScrollPane(bigImage);
                    scroll.setPreferredSize(new Dimension(400, 400));
                    JOptionPane.showMessageDialog(ChatWindow.this, scroll, "\uD83D\uDCF7 이미지: " + fileInfo.getFileName(), JOptionPane.PLAIN_MESSAGE);
                }
            });
            chatPanel.add(imgLabel);
        } else {
            JPanel row = new JPanel(new BorderLayout());
            row.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            row.add(new JLabel("\uD83D\uDCC1 " + fileInfo.getFileName()), BorderLayout.CENTER);
            JButton saveBtn = getSaveBtn(fileInfo);
            row.add(saveBtn, BorderLayout.EAST);
            chatPanel.add(row);
        }
        chatPanel.revalidate();
        chatPanel.repaint();
        scrollToBottom();
    }

    private JButton getSaveBtn(FileInfo fileInfo) {
        JButton saveBtn = new JButton("\uD83D\uDCE5 저장");
        saveBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File(fileInfo.getFileName()));
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Files.write(chooser.getSelectedFile().toPath(), fileInfo.getData());
                    JOptionPane.showMessageDialog(this, "파일 저장 완료!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "파일 저장 실패: " + ex.getMessage());
                }
            }
        });
        return saveBtn;
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
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
                            appendTextMessage("✅ 파일 전송 완료: " + selectedFile.getName());
                            selectedFile = null;
                            inputField.setEnabled(true);
                            inputField.setText("");
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(ChatWindow.this, "파일 전송 실패: " + ex.getMessage()));
                    }
                }).start();
            } else {
                String message = inputField.getText().trim();
                if (!message.isEmpty()) {
                    try {
                        client.getSender().sendText(message, user, roomId);
                        inputField.setText("");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(ChatWindow.this, "메시지 전송 실패: " + ex.getMessage());
                    }
                }
            }
        }
    }
}
