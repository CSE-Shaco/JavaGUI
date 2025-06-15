package client.gui.component;

import client.gui.util.WrapEditorKit;
import shared.dto.MessageResponse;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import java.awt.*;

public class ChatBubble extends JPanel {

    private static final int MAX_WIDTH = 320;

    public ChatBubble(MessageResponse response, boolean isMine) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);
        setBorder(BorderFactory.createLineBorder(Color.RED));  // ✅ ChatBubble 전체 테두리

        // 메시지 텍스트 구성
        String sender = response.getSender();
        String message = response.getMessage();
        String fullMessage = (response.isSystemMessage() ? "[System] " : (isMine ? "" : sender + " : ")) + message;

        // JTextPane 설정
        JTextPane textPane = new JTextPane();
        textPane.setEditorKit(new WrapEditorKit());
        textPane.setText(fullMessage);
        textPane.setEditable(false);
        textPane.setOpaque(true);
        textPane.setFont(new Font("SansSerif", response.isSystemMessage() ? Font.ITALIC : Font.PLAIN, 13));
        textPane.setBorder(BorderFactory.createLineBorder(Color.BLUE));  // ✅ 텍스트 패널 테두리
        textPane.setCaret(new DefaultCaret() {
            @Override
            public void paint(Graphics g) {}
        });

        // 정렬
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setAlignment(attr, response.isSystemMessage() ? StyleConstants.ALIGN_CENTER : isMine ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
        textPane.setParagraphAttributes(attr, true);

        // 배경색
        textPane.setBackground(isMine ? new Color(0xDFFFD6) : Color.WHITE);

        // 높이 측정
        textPane.setSize(MAX_WIDTH, Short.MAX_VALUE);
        textPane.revalidate();
        View rootView = textPane.getUI().getRootView(textPane);
        float preferredHeight = rootView.getPreferredSpan(View.Y_AXIS);
        Dimension size = new Dimension(MAX_WIDTH, (int) preferredHeight + 20);

        textPane.setPreferredSize(size);
        textPane.setMaximumSize(size);

        // 래퍼 패널
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createLineBorder(Color.GREEN));  // ✅ wrapper 패널 테두리
        wrapper.add(textPane, BorderLayout.CENTER);
        wrapper.setMaximumSize(size);
        wrapper.setPreferredSize(size);
        wrapper.setMinimumSize(new Dimension(50, textPane.getPreferredSize().height + 24));

        // 정렬 방식에 따라 위치 배치
        if (response.isSystemMessage()) {
            add(Box.createHorizontalGlue());
            add(wrapper);
            add(Box.createHorizontalGlue());
        } else if (isMine) {
            add(Box.createHorizontalGlue());
            add(wrapper);
        } else {
            add(wrapper);
            add(Box.createHorizontalGlue());
        }
    }
}