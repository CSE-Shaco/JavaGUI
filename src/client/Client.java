package client;

import client.gui.LandingPage;

public class Client {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(LandingPage::new);
    }
}