package client.gui;

import shared.domain.User;
import shared.dto.ClientRequest;
import shared.dto.MessageResponse;
import shared.dto.ServerResponse;

import javax.swing.*;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class WaitingWindow extends JFrame {

    private final User user;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public WaitingWindow(User user) {
        this.user = user;

        setTitle("랜덤 채팅 - 매칭 대기 중");
        setSize(300, 150);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel messageLabel = new JLabel("상대를 기다리는 중입니다...", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        add(messageLabel, BorderLayout.CENTER);

        JButton cancelButton = new JButton("취소");
        cancelButton.addActionListener(e -> cancelWaiting());
        add(cancelButton, BorderLayout.SOUTH);

        setVisible(true);
        startMatching(); // 매칭 시도 시작
    }

    private void startMatching() {
        new Thread(() -> {
            try {
                socket = new Socket("localhost", 12345); // 서버 포트
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                ClientRequest req = new ClientRequest("start_random" ,"", "", user, null);
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
                        JOptionPane.showMessageDialog(this, "서버와 연결 중 문제가 발생했습니다.")
                );
                dispose();
            }
        }).start();
    }

    private void cancelWaiting() {
        try {
            ClientRequest request = new ClientRequest("cancel_waiting", "" , "", user, null);
            out.writeObject(request);
            out.flush();
        } catch (Exception ignored) {
        } finally {
            dispose();
            new RoomSelectionWindow(user);
        }
    }

    private void openChatRoom(String roomId) {
        SwingUtilities.invokeLater(() -> {
            dispose();
            new ChatWindow(user, roomId);
        });
    }
}
