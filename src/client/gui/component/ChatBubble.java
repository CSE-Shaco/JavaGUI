package client.gui.component;

import shared.dto.MessageResponse;

import javax.swing.*;
import java.awt.*;

/**
 * ChatBubble represents a single chat message block in the chat UI.
 * It supports different styles for self, others, and system messages.
 */
public class ChatBubble extends JPanel {

    /**
     * Constructs a ChatBubble with appropriate alignment and styling.
     *
     * @param response Message data
     * @param isMine   Whether the message was sent by the current user
     */
    public ChatBubble(MessageResponse response, boolean isMine) {
        int baseWidth = 200;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);

        String sender = response.getSender();
        String message = insertSoftBreaks(response.getMessage(), 10);
        boolean isSystem = response.isSystemMessage();

        // Temporary label for measuring width
        JLabel preLabel = new JLabel(buildStyledHtml(sender, message, isMine, isSystem, 0));
        int measuredWidth = preLabel.getPreferredSize().width - 10;
        int finalWidth = Math.min(measuredWidth, baseWidth);

        // Main label with adjusted width
        JLabel bubbleLabel = isSystem
                ? preLabel
                : new JLabel(buildStyledHtml(sender, message, isMine, false, finalWidth));

        int height = bubbleLabel.getPreferredSize().height;

        // Set fixed height for layout consistency
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, height + 16));

        Dimension labelSize = bubbleLabel.getPreferredSize();

        // Message wrapper panel aligned based on message type
        JPanel wrapper = new JPanel(new FlowLayout(
                isSystem ? FlowLayout.CENTER : isMine ? FlowLayout.RIGHT : FlowLayout.LEFT
        ));
        wrapper.setOpaque(false);
        wrapper.add(bubbleLabel);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, labelSize.height));

        add(wrapper);
    }

    /**
     * Inserts <wbr> tags into long words to improve HTML line breaking.
     *
     * @param text      Original message
     * @param threshold Max characters before breaking
     * @return Message with soft breaks
     */
    public static String insertSoftBreaks(String text, int threshold) {
        StringBuilder result = new StringBuilder();

        for (String word : text.split(" ")) {
            if (containsKorean(word) || word.length() <= threshold) {
                result.append(word);
            } else {
                result.append(insertBreaks(word, threshold));
            }
            result.append(" ");
        }

        return result.toString().trim();
    }

    /**
     * Checks whether the given word contains Korean characters.
     * Used to avoid breaking Korean syllables.
     */
    private static boolean containsKorean(String word) {
        for (char ch : word.toCharArray()) {
            if (ch >= 0xAC00 && ch <= 0xD7A3) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inserts <wbr> tags every 'threshold' characters into a long word.
     */
    private static String insertBreaks(String word, int threshold) {
        StringBuilder broken = new StringBuilder();
        int count = 0;

        for (char ch : word.toCharArray()) {
            broken.append(ch);
            count++;
            if (count == threshold) {
                broken.append("<wbr>");
                count = 0;
            }
        }

        return broken.toString();
    }

    /**
     * Constructs styled HTML for the chat bubble with inline CSS.
     *
     * @param sender   Sender's name
     * @param message  Message content
     * @param isMine   Whether it's sent by the current user
     * @param isSystem Whether it's a system message
     * @param width    Max width (0 for auto)
     * @return HTML string to use in JLabel
     */
    public static String buildStyledHtml(String sender, String message, boolean isMine, boolean isSystem, int width) {
        String prefix = isSystem ? "[System] " : isMine ? "" : sender + " : ";
        String full = prefix + message;

        String align = isSystem ? "center" : "left";
        String bg = isMine ? "#DFFFD6" : !isSystem ? "#DDDDDD" : "#FFFFFF";
        String fontStyle = isSystem ? "italic" : "normal";
        String widthStyle = (width > 0) ? "width:" + width + "px;" : "";

        return "<html><div style='" +
                widthStyle +
                "text-align:" + align + ";" +
                "background-color:" + bg + ";" +
                "font-style:" + fontStyle + ";" +
                "padding:8px;" +
                "'>" + full + "</div></html>";
    }
}
