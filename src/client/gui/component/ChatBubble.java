package client.gui.component;

import shared.dto.MessageResponse;

import javax.swing.*;
import java.awt.*;

public class ChatBubble extends JPanel {

    public ChatBubble(MessageResponse response, boolean isMine) {
        int baseWidth = 200;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);

        String sender = response.getSender();
        String message = insertSoftBreaks(response.getMessage(), 10);
        boolean isSystem = response.isSystemMessage();

        JLabel preLabel = new JLabel(buildStyledHtml(sender, message, isMine, isSystem, 0));
        int measuredWidth = (int) ((preLabel.getPreferredSize().width - 16) / 1.5);
        int finalWidth = Math.min(measuredWidth, baseWidth);

        JLabel bubbleLabel = isSystem ? preLabel : new JLabel(buildStyledHtml(sender, message, isMine, isSystem, finalWidth));
        int height = bubbleLabel.getPreferredSize().height;
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, height + 16)); // ✅ 이 줄 매우 중요

        Dimension labelSize = bubbleLabel.getPreferredSize();

        // ✅ 말풍선 wrapper
        JPanel wrapper = new JPanel(new FlowLayout(isSystem ? FlowLayout.CENTER : isMine ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        wrapper.add(bubbleLabel);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, labelSize.height));

        add(wrapper);
    }

    public static String insertSoftBreaks(String text, int threshold) {
        StringBuilder sb = new StringBuilder();
        for (String word : text.split(" ")) {
            if (word.length() > threshold) {
                for (int i = 0; i < word.length(); i++) {
                    sb.append(word.charAt(i));
                    if ((i + 1) % threshold == 0) {
                        sb.append("<wbr>"); // soft wrap
                    }
                }
            } else {
                sb.append(word);
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

    public static String buildStyledHtml(String sender, String message, boolean isMine, boolean isSystem, int width) {
        String prefix = isSystem ? "[System] " : isMine ? "" : sender + " : ";
        String full = prefix + message;
        String align = isSystem ? "center" : "left";
        String bg = isMine ? "#DFFFD6" : "#FFFFFF";
        String fontStyle = isSystem ? "italic" : "normal";
        String widthStyle = (width > 0) ? "width:" + width + "px;" : "";

        return "<html><div style='" + widthStyle + "text-align:" + align + ";" + "background-color:" + bg + ";" + "font-style:" + fontStyle + ";" + "padding:8px;" + "'>" + full + "</div></html>";
    }
}