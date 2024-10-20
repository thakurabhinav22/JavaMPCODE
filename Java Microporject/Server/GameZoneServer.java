package Server;

import Server.ServerSecurity.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.*;

import javax.swing.*;
import java.awt.*;

public class GameZoneServer {
    public static void main(String[] args) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Enter Password:");
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(label, BorderLayout.WEST);
        panel.add(passwordField, BorderLayout.CENTER);

        int option = JOptionPane.showConfirmDialog(null, panel, "Authentication", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            char[] password = passwordField.getPassword();
            String passwordStr = new String(password);
            adminAuth auth = new adminAuth();
            boolean isAuth = auth.authenticate(passwordStr); 

            if (isAuth) {
                // JOptionPane.showMessageDialog(null, "Authentication Successful!");
                new ServerUi(); 
            } else {
                JOptionPane.showMessageDialog(null, "Authentication Failed", "Invalid Password. Please Try Again.", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Authentication Canceled", "Operation Canceled", JOptionPane.WARNING_MESSAGE);
        }
    }
}


class ServerUi {
    private JTextArea serverOperation;
    private StartServer startServer;
    private JButton serverOperationButton;
    private Thread ticTacToeThread;
    private Thread RockPaperScissorServerThread;

    public ServerUi() {
        int randomKey = new Random().nextInt(900000) + 10000;
        System.out.println("From Server: " + randomKey);
        // Create the top panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.BLACK); // Top Panel Color
        topPanel.setPreferredSize(new Dimension(700, 50));
        topPanel.setLayout(new GridBagLayout()); // Use GridBagLayout for centered layout

        // Create the server operation button
        serverOperationButton = new JButton("Start Server");
        serverOperationButton.setPreferredSize(new Dimension(150, 30)); // Set button size
        serverOperationButton.setBackground(Color.GREEN); // Set button background color
        serverOperationButton.setForeground(Color.WHITE); // Set button text color
        serverOperationButton.setFocusPainted(false);
        serverOperationButton.setFont(new Font("Arial", Font.BOLD, 16)); // Set button font

        // Create command input box
        JTextField cmdField = new JTextField();
        cmdField.setPreferredSize(new Dimension(500, 30)); // Set input field size

        // Create command send button
        JButton sendCmd = new JButton("Send");
        sendCmd.setPreferredSize(new Dimension(150, 30)); // Set button size
        sendCmd.setBackground(Color.DARK_GRAY); // Set send button color
        sendCmd.setForeground(Color.WHITE); // Set send button text color
        sendCmd.setFont(new Font("Arial", Font.PLAIN, 14)); // Set send button font

        // Server Operation View
        Font serverFont = new Font("Consolas", Font.BOLD, 15);
        serverOperation = new JTextArea();
        serverOperation.setFont(serverFont);
        serverOperation.setBackground(Color.BLACK); // Set background to black
        serverOperation.setForeground(Color.WHITE); // Set text color to white
        serverOperation.setEditable(false);
        serverOperation.setLineWrap(true); // Optional: Wrap lines for better readability

        // Command Panel Creation
        JPanel commandPanel = new JPanel(new GridBagLayout());
        commandPanel.setPreferredSize(new Dimension(700, 50));
        commandPanel.setBackground(Color.BLACK);

        // Add components to the command panel with proper alignment
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding around components

        // Add components to topPanel
        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(serverOperationButton, gbc);

        // Add components to commandPanel
        gbc.gridx = 0;
        gbc.gridy = 0;
        commandPanel.add(cmdField, gbc);
        gbc.gridx = 1;
        commandPanel.add(sendCmd, gbc);

        // Create the main frame
        JFrame frame = new JFrame("Server");
        frame.setSize(700, 1000);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.BLACK);

        // Create a JScrollPane without borders
        JScrollPane scrollPane = new JScrollPane(serverOperation);
        scrollPane.setBorder(null); // Remove border from JScrollPane

        // Add panels to the frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(commandPanel, BorderLayout.SOUTH);
        frame.add(scrollPane, BorderLayout.CENTER); // Use the customized JScrollPane

        // Handle frame close to stop server
        // frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                if (startServer != null) {
                    startServer.stopServer();
                }
                if (ticTacToeThread != null && ticTacToeThread.isAlive()) {
                    TicTacToeServer.stopServer();
                }
                if (RockPaperScissorServerThread != null && RockPaperScissorServerThread.isAlive()) {
                    TicTacToeServer.stopServer();
                }
                frame.dispose();
            }
        });

        // Make the frame visible
        frame.setVisible(true);

        serverOperationButton.addActionListener(new ActionListener() {
           
            public void actionPerformed(ActionEvent e) {
                RockPaperScissorServer server = new RockPaperScissorServer(serverOperation);
                if (startServer == null || !startServer.isRunning()) {
                    startServer = new StartServer(serverOperation, randomKey);
                    serverOperationButton.setBackground(Color.RED);
                    serverOperationButton.setText("Stop Server");
        
                    // Start Tic Tac Toe server in a separate thread only if it's not already running
                    if (ticTacToeThread == null || !ticTacToeThread.isAlive()) {
                        ticTacToeThread = new Thread(() -> new TicTacToeServer(serverOperation));
                        ticTacToeThread.start();
                    }
        
                    // // Start Rock Paper Scissor server in a separate thread only if it's not already running
                    // if (RockPaperScissorServerThread == null || !RockPaperScissorServerThread.isAlive()) {
                    //     RockPaperScissorServerThread = new Thread(() -> new RockPaperScissorServer());
                    //     RockPaperScissorServerThread.start();
                    // }
        
                } else {
                    // Stop main server
                    startServer.stopServer();
                    startServer = null;
                    serverOperationButton.setBackground(Color.GREEN);
                    serverOperationButton.setText("Start Server");
        
                    // Stop Tic Tac Toe server
                    if (ticTacToeThread != null && ticTacToeThread.isAlive()) {
                        TicTacToeServer.stopServer();
                        ticTacToeThread = null;
                        server.stopServer();
                    }
        
                    // Stop Rock Paper Scissor server
                    if (RockPaperScissorServerThread != null && RockPaperScissorServerThread.isAlive()) {
                        server.stopServer();                        
                        RockPaperScissorServerThread = null;
                    }
                }
            }
        });
        

        sendCmd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Handle commands sent from the UI
                // You might want to add specific commands handling here
            }
        });
    }
}

class StartServer extends Thread {
    private ServerSocket serverSocket;
    private JTextArea serverOperation;
    private volatile boolean running = true;
    private int secretKey;


    public StartServer(JTextArea serverOperation, int secretKey) {
        this.serverOperation = serverOperation;
        this.secretKey = secretKey;
        this.start(); // Start the server thread
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(6666);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(" dd-MM-yy HH:mm:ss");
            String currentTime = LocalDateTime.now().format(formatter);

            SwingUtilities.invokeLater(() -> serverOperation.append("Server Connection opened at: " + currentTime + "\n"));

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    InetAddress clientAddress = clientSocket.getInetAddress(); // Get client IP address
                    String clientIP = clientAddress.getHostAddress();
                    Encryptor secureIp = new Encryptor();
                    String EncIp = secureIp.EncuserId(clientIP);

                    SwingUtilities.invokeLater(() -> serverOperation.append("\nUser Connected: " + EncIp + "\n"));

                    DataOutputStream sendSecretKey = new DataOutputStream(clientSocket.getOutputStream());
                    sendSecretKey.writeInt(secretKey);

                    new MessageHandler(clientSocket, serverOperation);

                } catch (IOException e) {
                    if (running) {
                        SwingUtilities.invokeLater(() -> serverOperation.append("Error: " + e.getMessage()));
                    }
                }
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> serverOperation.append("Error: " + e.getMessage()));
            e.printStackTrace();
        } finally {
            stopServer();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                SwingUtilities.invokeLater(() -> serverOperation.append("Server Connection Closed\n"));
                RockPaperScissorServer RPSServer = new RockPaperScissorServer(serverOperation);
                RPSServer.stopServer();
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> serverOperation.append("\nError closing server: " + e.getMessage()));
            e.printStackTrace();
        }
    }
}

class MessageHandler extends Thread {
    private Socket clientSocket;
    private JTextArea serverOperation;
    private Encryptor secureIp;

    public MessageHandler(Socket clientSocket, JTextArea serverOperation) {
        this.clientSocket = clientSocket;
        this.serverOperation = serverOperation;
        this.secureIp = new Encryptor();
        start();
    }

    public void run() {
        try {
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());
            String clientIP = clientSocket.getInetAddress().getHostAddress();
            String EncIp = secureIp.EncuserId(clientIP);
            DataOutputStream SendMessage = new DataOutputStream(clientSocket.getOutputStream());

            while (true) {
                String message = input.readUTF();
                SwingUtilities.invokeLater(
                        () -> serverOperation.append("\nMessage By User " + EncIp + ": " + message + "\n"));
                SendMessage.writeUTF("Message Sended to user: " + message);
            }
        } catch (IOException e) {
            // Handle disconnection
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class TicTacToeServer {
    private static final int PORT = 6667;
    private static final List<Socket> players = new ArrayList<>();
    private static ServerSocket serverSocket;
    private static boolean running = true;
    Socket playerSocket; 

    public TicTacToeServer(JTextArea operationPanel) {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Tic Tac Toe Server started on port " + PORT );
            SwingUtilities.invokeLater(() ->operationPanel.append("Tic Tac Toe Server started on port: "+ PORT+ "\n"));

            while (running) {
                try {
                    Socket playerSocket = serverSocket.accept();
                    this.playerSocket = playerSocket;
                    synchronized (players) {
                        players.add(playerSocket);
                    }
                    // Start a new thread to handle the game logic for each player
                    new Thread(new TicTacToeGameHandler(playerSocket)).start();
                } catch (IOException e) {
                    if (!running) {
                        System.out.println("Tic Tac Toe Server is stopping...");
                         stopServer();
                        break;
                    } else {
                        System.err.println("Tic Tac Toe Server error: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Tic Tac Toe Server error: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    public static void stopServer() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Tic Tac Toe Server stopped.");
            }
        } catch (IOException e) {
            System.err.println("Error closing Tic Tac Toe Server: " + e.getMessage());
        }
    }

    private static class TicTacToeGameHandler implements Runnable {
        private final Socket playerSocket;
        private String playerMove; // To track each player's move (X or O)
        private final DataInputStream in;
        private final DataOutputStream out;

        TicTacToeGameHandler(Socket socket) throws IOException {
            this.playerSocket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            // Send initial player symbol (X or O) to the client
            if (players.size() % 2 == 0) {
                playerMove = "X";
                out.writeUTF("X"); // Assign X to the new player
            } else {
                playerMove = "O";
                out.writeUTF("O"); // Assign O to the new player
            }
            out.flush();

            // Start a new thread to handle this player's moves
            new Thread(this).start();
        }

        public void run() {
            try {
                while (true) {
                    String move = in.readUTF();
                    System.out.println("Move received from player " + playerMove + ": " + move);
                    broadcastMove(move); // Broadcast the move to all players
                }
            } catch (EOFException e) {
                System.err.println("Connection closed by player: " + playerMove);
            } catch (IOException e) {
                System.err.println("IO Exception: " + e.getMessage());
            } finally {
                // Remove the player and close the socket when done
                synchronized (players) {
                    players.remove(playerSocket);
                }
                try {
                    playerSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        private void broadcastMove(String move) throws IOException {
            synchronized (players) {
                for (Socket socket : players) {
                    if (socket != playerSocket) {
                        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                        out.writeUTF(move);
                        out.flush();
                    }
                }
            }
        }
    }
}

class RockPaperScissorServer extends Thread {
    private static final int PORT = 6668;
    private final List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();
    private final Map<ClientHandler, String> clientMoves = new HashMap<>();
    private ServerSocket serverSocket;
    private volatile boolean isRunning = true;
    JTextArea operationArea;
    RockPaperScissorServer(JTextArea operationArea) {
        this.operationArea = operationArea;
        start();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Rock Paper Scissors Server started on port " + PORT);
            operationArea.append("Rock Paper Scissors Server started on port: " + PORT+ "\n");
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandlers.add(clientHandler);
                    new Thread(clientHandler).start();
                } catch (IOException e) {
                    if (!isRunning) {
                        System.out.println("Rock Paper Scissors Server is stopping...");
                        break;
                    } else {
                        System.err.println("Rock Paper Scissors Server error: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Rock Paper Scissors Server error: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Rock Paper Scissors Server stopped.");
            }
            for (ClientHandler handler : clientHandlers) {
                handler.closeConnection();
            }
            clientHandlers.clear();
        } catch (IOException e) {
            System.err.println("Error closing Rock Paper Scissors Server: " + e.getMessage());
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;
        private volatile boolean isRunning = true;
        private static final String PLAYER_1 = "Player 1";
        private static final String PLAYER_2 = "Player 2";
        private static Map<ClientHandler, String> clientMoves = new HashMap<>();
        private static List<ClientHandler> clientHandlers = new ArrayList<>();
        
        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
        }
    
        @Override
        public void run() {
            try {
                while (isRunning) {
                    String move = in.readUTF();
                    synchronized (clientMoves) {
                        clientMoves.put(this, move);
                        if (clientMoves.size() == 2) {
                            determineRoundWinner();
                            clientMoves.clear(); // Clear moves after determining the winner
                        }
                    }
                }
            } catch (IOException e) {
                if (!isRunning) {
                    System.out.println("Client disconnected.");
                } else {
                    System.err.println("Client handler error: " + e.getMessage());
                }
            } finally {
                closeConnection();
            }
        }
    
        public void closeConnection() {
            isRunning = false;
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
    
        private void determineRoundWinner() {
            if (clientMoves.size() == 2) {
                List<ClientHandler> handlers = new ArrayList<>(clientMoves.keySet());
                String move1 = clientMoves.get(handlers.get(0));
                String move2 = clientMoves.get(handlers.get(1));
    
                String result1 = getResultMessage(move1, move2, handlers.get(0));
                String result2 = getResultMessage(move2, move1, handlers.get(1));
                
                // Send result messages to each client
                sendMessage(handlers.get(0), result1);
                sendMessage(handlers.get(1), result2);
            }
        }
    
        private String getResultMessage(String myMove, String opponentMove, ClientHandler myHandler) {
            if (myMove.equals(opponentMove)) {
                return "It's a tie! You both selected " + myMove;
            }
    
            String result;
            switch (myMove) {
                case "Rock":
                    result = opponentMove.equals("Scissors") ? "You win!" : "You lose!";
                    break;
                case "Paper":
                    result = opponentMove.equals("Rock") ? "You win!" : "You lose!";
                    break;
                case "Scissors":
                    result = opponentMove.equals("Paper") ? "You win!" : "You lose!";
                    break;
                default:
                    result = "Invalid move.";
                    break;
            }
    
            return result + " Opponent selected " + opponentMove;
        }
    
        private void sendMessage(ClientHandler handler, String message) {
            try {
                handler.out.writeUTF(message);
                handler.out.flush();
            } catch (IOException e) {
                System.err.println("Error sending message: " + e.getMessage());
            }
        }
    }
}