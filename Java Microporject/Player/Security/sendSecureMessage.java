package Player.Security;
import java.net.*;
import java.io.*;
import javax.swing.*;

public class sendSecureMessage {

    private static Socket socket;
    private static DataOutputStream outputStream;

    public sendSecureMessage(String message) {
        try {
            if (socket == null || socket.isClosed()) {
                try {
                    // Attempt to establish a connection
                    socket = new Socket("localhost", 6666);
                    outputStream = new DataOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    showErrorMessage("Unable to connect to server: " + e.getMessage());
                    return; // Exit if connection cannot be established
                }
            }

            // Send the message
            outputStream.writeUTF(message);
            outputStream.flush();
        } catch (IOException e) {
            showErrorMessage("Failed to send message: " + e.getMessage());
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            showErrorMessage("Error closing connection: " + e.getMessage());
        }
    }

    private void showErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, "Connection Error", JOptionPane.ERROR_MESSAGE);
        });
    }
}
