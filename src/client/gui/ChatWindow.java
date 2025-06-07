package client.gui;

import client.core.ChatClient;
import shared.domain.FileInfo;
import shared.domain.User;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;

public class ChatWindow extends JFrame {

    private final User user;
    private final String roomId;
    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JButton sendButton;
    private final JButton fileButton;
    private final ChatClient client;
    private File selectedFile = null;

    public ChatWindow(User user, String roomId) {
        this.user = user;
        this.roomId = roomId;

        setTitle("Chat - " + user.getDisplayName());
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFocusable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        sendButton = new JButton("Send");
        fileButton = new JButton("파일 선택");

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(fileButton, BorderLayout.WEST);
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(new SendHandler());
        inputField.addActionListener(new SendHandler());
        fileButton.addActionListener(e -> handleFileSelection());

        client = new ChatClient("localhost", 12345, user, roomId, chatArea);

        setVisible(true);
    }

    private void handleFileSelection() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("전송할 파일을 선택하세요");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("모든 파일", "*"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            inputField.setEnabled(false);
            chatArea.append("📎 파일 전송 대기 중: " + selectedFile.getName() + "\n");
        }
    }

    public void handleReceivedFile(FileInfo fileInfo) {
        if (fileInfo.isImage()) {
            // 이미지 미리보기 팝업
            ImageIcon icon = new ImageIcon(fileInfo.getData());
            JLabel imageLabel = new JLabel(icon);
            JScrollPane scrollPane = new JScrollPane(imageLabel);
            scrollPane.setPreferredSize(new Dimension(400, 400));
            JOptionPane.showMessageDialog(this, scrollPane, "📷 수신된 이미지: " + fileInfo.getFileName(), JOptionPane.PLAIN_MESSAGE);
        } else {
            // 일반 파일 수신 안내
            JButton saveBtn = new JButton("📥 저장");
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

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("📁 수신된 파일: " + fileInfo.getFileName()), BorderLayout.CENTER);
            panel.add(saveBtn, BorderLayout.EAST);
            JOptionPane.showMessageDialog(this, panel, "파일 수신", JOptionPane.PLAIN_MESSAGE);
        }
    }

    private class SendHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedFile != null) {
                // 파일 전송 처리
                new Thread(() -> {
                    try {
                        byte[] fileData = Files.readAllBytes(selectedFile.toPath());
                        boolean isImage = selectedFile.getName().matches("(?i).+\\.(png|jpg|jpeg|gif)$");
                        FileInfo fileInfo = new FileInfo(selectedFile.getName(), fileData, isImage);

                        client.getFileSender().sendFile(fileInfo, user, roomId);

                        SwingUtilities.invokeLater(() -> {
                            chatArea.append("✅ 파일 전송 완료: " + selectedFile.getName() + "\n");
                            selectedFile = null;
                            inputField.setEnabled(true);
                            inputField.setText("");
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(ChatWindow.this, "파일 전송 실패: " + ex.getMessage())
                        );
                    }
                }).start();
            } else {
                // 텍스트 메시지 전송 처리
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