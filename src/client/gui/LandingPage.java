package client.gui;

import shared.domain.User;

import javax.swing.*;
import java.awt.*;

public class LandingPage extends JFrame {

    public LandingPage() {
        setTitle("채팅 프로그램 - 로그인");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();

        JLabel displayNameLabel = new JLabel("Display Name:");
        JTextField displayNameField = new JTextField();

        JButton enterButton = new JButton("입장");

        add(usernameLabel);
        add(usernameField);
        add(displayNameLabel);
        add(displayNameField);
        add(enterButton);

        enterButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String displayName = displayNameField.getText().trim();

            if (username.isEmpty() || displayName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 정보를 입력해주세요.");
                return;
            }

            User user = new User(username, displayName);
            System.out.println(">>> [LandingPage] 입장 버튼 클릭됨"); // 이 로그 추가

            new RoomSelectionPage(user); // ✅ 여기로 변경
            dispose(); // 현재 창 닫기
        });

        setVisible(true);
    }
}