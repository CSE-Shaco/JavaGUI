package client;

import client.gui.LandingWindow;

public class Client {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(LandingWindow::new);
    }
}