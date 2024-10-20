package Xo;

import java.io.*;
import java.net.*;
import java.util.*;

public class GameServer {
    private static final int PORT = 6666;
    private static final List<Socket> players = new ArrayList<>();
    private static final List<DataOutputStream> outStreams = new ArrayList<>();
    private static final List<DataInputStream> inStreams = new ArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started");

            // Accept connections from two players
            while (players.size() < 2) {
                Socket playerSocket = serverSocket.accept();
                players.add(playerSocket);
                DataOutputStream out = new DataOutputStream(playerSocket.getOutputStream());
                DataInputStream in = new DataInputStream(playerSocket.getInputStream());
                outStreams.add(out);
                inStreams.add(in);
                System.out.println("Player connected: " + playerSocket.getInetAddress());
            }

            // Assign roles and start handling players
            assignRoles();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void assignRoles() throws IOException {
        // Notify players of their roles
        outStreams.get(0).writeUTF("X");
        outStreams.get(1).writeUTF("O");

        new Thread(new PlayerHandler(0)).start();
        new Thread(new PlayerHandler(1)).start();
    }

    private static class PlayerHandler implements Runnable {
        private final int playerIndex;

        PlayerHandler(int index) {
            this.playerIndex = index;
        }

        @Override
        public void run() {
            try {
                DataInputStream in = inStreams.get(playerIndex);
                while (true) {
                    String move = in.readUTF();
                    sendMoveToOpponents(move);
                }
            } catch (IOException e) {
                System.err.println("Connection lost: " + e.getMessage());
            } finally {
                // Close the socket when done
                try {
                    players.get(playerIndex).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendMoveToOpponents(String move) throws IOException {
            for (int i = 0; i < players.size(); i++) {
                if (i != playerIndex) {
                    DataOutputStream out = outStreams.get(i);
                    out.writeUTF(move);
                    out.flush(); // Ensure data is sent immediately
                }
            }
        }
    }
}
