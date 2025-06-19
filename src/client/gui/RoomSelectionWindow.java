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

public class RoomSelectionWindow extends JFrame {

    private final User user;
    private final JPanel roomListPanel;

    public RoomSelectionWindow(User user) {
        this.user = user;

        System.out.println(">>> [RoomeSelectionPage] 생성자 진입");
        setTitle("참여할 채팅방을 선택하세요 - " + user.getDisplayName());
        setSize(480, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        roomListPanel = new JPanel();
        roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(roomListPanel);

        JPanel buttonPanel = new JPanel();
        JButton createButton = new JButton("새 방 생성");
        JButton randomButton = new JButton("랜덤 채팅");
        JButton refreshButton = new JButton("새로고침");

        buttonPanel.add(createButton);
        buttonPanel.add(randomButton);
        buttonPanel.add(refreshButton);

        add(new JLabel("현재 참여 가능한 채팅방:"), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        createButton.addActionListener(e -> {
            String roomId = JOptionPane.showInputDialog(this, "새 방 이름:");
            if (roomId != null && !roomId.trim().isEmpty()) openChatWindow(roomId.trim());
        });

        randomButton.addActionListener(e -> {
            new WaitingWindow(user); // ✅ WaitingWindow 띄우기
            dispose();               // 현재 창 닫기
        });


        refreshButton.addActionListener(e -> fetchRoomList());

        fetchRoomList();
        setVisible(true);
    }

    private void fetchRoomList() {
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 12347);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

                out.writeObject(new RoomListRequest());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                RoomListFetcher fetcher = new RoomListFetcher(socket, in, roomMap -> SwingUtilities.invokeLater(() -> updateRoomPanels(roomMap)));
                fetcher.start();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "방 목록을 불러올 수 없습니다."));
            }
        }).start();
    }

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

    private JPanel createRoomPanel(String roomId, int participantCount) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(440, 60));
        panel.setMaximumSize(new Dimension(440, 60));
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(roomId + "  (인원: " + participantCount + ")");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JButton enterButton = new JButton("입장");
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

    private void openChatWindow(String roomId) {
        new ChatWindow(user, roomId);
        dispose();
    }
}