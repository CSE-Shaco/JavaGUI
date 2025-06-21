package client;

import client.gui.LandingWindow;

/**
 * Entry point for the client application.
 */
public class Client {

    public static void main(String[] args) {
        // Launch the GUI on the Event Dispatch Thread
        javax.swing.SwingUtilities.invokeLater(LandingWindow::new);
    }
}
