package Player;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Player.Security.Secure;
// import Player.Games.TicTaToeGameConn;

public class GameZonePlayer {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ServerConnection conn = new ServerConnection();
                int secretKey = conn.connect();

                if (secretKey == -1) {
                    showErrorMessage("Server Refused to Connect");
                    new UIBuilder(secretKey);
                } else {
                    new UIBuilder(secretKey); 
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void showErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, "Connection Error", JOptionPane.ERROR_MESSAGE);
        });
    }
}

class ServerConnection {
    private Socket socket;

    public int connect() {
        try {
            socket = new Socket("localhost", 6666); // Use actual server address if needed
            DataInputStream keyStream = new DataInputStream(socket.getInputStream());
            int secretKey = keyStream.readInt();
            System.out.println("SecretKey got from server: " + secretKey);
            return secretKey;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Socket getSocket() {
        return socket;
    }
}

class UIBuilder {
    int key;

    UIBuilder(int key) {
        this.key = key;
        System.out.println("UiBuild Passed Seceret key: " + key);
        JFrame frame = new JFrame("GameZone");
        frame.setLayout(new BorderLayout());

        JPanel sideBar = new JPanel();
        sideBar.setPreferredSize(new Dimension(300, 1000));
        sideBar.setBackground(new Color(0, 0, 51));
        sideBar.setLayout(new BorderLayout());

        JPanel imgContainer = new JPanel();
        imgContainer.setLayout(new GridBagLayout());
        imgContainer.setBackground(new Color(0, 0, 51));

        JPanel titlePanel = new JPanel(new GridBagLayout());
        titlePanel.setBackground(new Color(0, 0, 51));
        titlePanel.setPreferredSize(new Dimension(1000, 120));

        JLabel gameZoneTitle = new JLabel("GameZone");
        gameZoneTitle.setFont(new Font("Showcard Gothic", Font.BOLD, 50));
        gameZoneTitle.setForeground(Color.YELLOW);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 0, 0, 0);

        titlePanel.add(gameZoneTitle, gbc);

        JLabel imageLabel = new JLabel();
        ImageIcon imageIcon = new ImageIcon("./assets/HomeScreen.png");
        imageLabel.setIcon(imageIcon);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        imgContainer.add(imageLabel, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setBackground(new Color(0, 0, 51));

        gbc.insets = new Insets(10, 10, 10, 10);

        JButton ticTacToeButton = createStyledButton("Tic Tac Toe", Color.GREEN);
        JButton rockPaperScissorsButton = createStyledButton("Rock Paper Scissors", Color.BLUE);

        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(ticTacToeButton, gbc);

        gbc.gridy = 1;
        buttonPanel.add(rockPaperScissorsButton, gbc);

        sideBar.add(buttonPanel, BorderLayout.CENTER);

        frame.add(sideBar, BorderLayout.WEST);
        frame.add(imgContainer, BorderLayout.CENTER);
        frame.add(titlePanel, BorderLayout.NORTH);

        frame.setVisible(true);
        frame.setSize(1500, 1000);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        System.out.println("Key passed to TicToeGae from Ui Builder: " + key);
        ticTacToeButton.addActionListener(e -> new TicTaToeGameConn(key, frame));
        rockPaperScissorsButton.addActionListener(e-> new RockPaperScissor(frame,key));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(200, 50));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return button;
    }
    
}

class TicTaToeGameConn {
    private JButton[] buttons = new JButton[9];
    private Socket s;
    private String currentPlayerMove = "";
    private boolean isFirstPlayer = false;
    private boolean isMyTurn = false;
    private int key;
    private JTextArea chatArea;
    private JTextField chatInput;
    private DataOutputStream out;
    JFrame f = new JFrame();

    Secure secure = new Secure();

    public TicTaToeGameConn(int tkey, JFrame previousFrame) {
        this.key = tkey;
        if (previousFrame != null) {
            previousFrame.dispose();
        }
        try {
            this.s = new Socket("localhost", 6667);
            System.out.println("TicTacToeGame key: " + key);
        } catch (Exception e) {
            showErrorMessage("Server Refused to connect");
            return;
        }

        createAndShowGUI(f);

        try {
            out = new DataOutputStream(s.getOutputStream());
            new Thread(new IncomingMessages()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createAndShowGUI(JFrame f) {
        // Home button with confirmation on leave
        JButton homeButton = new JButton();
        homeButton = createStyledButton("Leave", Color.RED);

        // Create top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(100, 50));
        topPanel.setBackground(new Color(0, 0, 51));

        topPanel.add(homeButton, BorderLayout.WEST);

        // Create Tic Tac Toe board panel
        JPanel tictactoeBoard = new JPanel(new GridLayout(3, 3, 3, 3));
        tictactoeBoard.setPreferredSize(new Dimension(500, 500));

        // Initialize buttons and add them to the board
        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton();
            buttons[i].setFont(new Font("Arial", Font.BOLD, 60));
            buttons[i].addActionListener(new ButtonClickListener(i));
            tictactoeBoard.add(buttons[i]);
        }

        // Create chat panel
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(300, 500));
        chatPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        // Create chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Create chat input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        // Create chat input box
        chatInput = new JTextField();
        inputPanel.add(chatInput, BorderLayout.CENTER);

        // Create send button
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // Create and set up the frame
        f.setTitle("Player");
        f.setLayout(new BorderLayout());

        f.add(topPanel, BorderLayout.NORTH);
        f.add(tictactoeBoard, BorderLayout.CENTER);
        f.add(chatPanel, BorderLayout.EAST);

        f.setSize(1500, 1000);
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        f.setVisible(true);

        // Window close event handling
        f.addWindowListener(new WindowAdapter() {
           
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showConfirmDialog(f, "Are you sure you want to leave the game?",
                        "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        if (out != null) {
                            out.writeUTF("PLAYER_LEFT:" + currentPlayerMove); // Notify the server and opponent
                            out.flush();
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    f.dispose();
                    new UIBuilder(key); // Return to home screen
                }
            }
        });

        // Leave button event handling
        homeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(f, "Are you sure you want to leave the game?",
                        "Confirm Leave", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        if (out != null) {
                            out.writeUTF("PLAYER_LEFT:" + currentPlayerMove); // Notify the server and opponent
                            out.flush();
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    f.dispose();
                    new UIBuilder(key); // Return to home screen
                }
            }
        });
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(200, 50));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        return button;
    }

    private static void showErrorMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, "Connection Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    private class ButtonClickListener implements ActionListener {
        private int index;

        public ButtonClickListener(int index) {
            this.index = index;
        }

       
        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();
            if (source.getText().isEmpty() && isMyTurn && 
                ((isFirstPlayer && "X".equals(currentPlayerMove)) || 
                 (!isFirstPlayer && "O".equals(currentPlayerMove)))) {
                source.setText(currentPlayerMove);
                System.out.println("Button clicked: " + index);
                setMove(index);
                isMyTurn = false; // End the current player's turn
                disableMyButtons(); // Disable buttons for the current player
                if (checkForWin()) {
                    displayWinner("Player " + currentPlayerMove + " wins!");
                } else if (checkForDraw()) {
                    displayWinner("The game is a draw!");
                }
            } else {
                System.out.println("Invalid move attempt by player: " + currentPlayerMove);
            }
        }
    }

    void setMove(int index) {
        try {
            out.writeUTF("MOVE:" + index + currentPlayerMove);
            out.flush(); // Ensure data is sent immediately
        } catch (IOException e) {
            System.out.println("Error sending move: " + e.getMessage());
        }
    }

    void getMove(String move) {
        String decMessString = secure.DecryptMessage(move, key);
        if (move.startsWith("MOVE:")) {
            String moveData = move.substring(5); // Remove "MOVE:" prefix
            int pos = Integer.parseInt(moveData.substring(0, moveData.length() - 1));
            String value = moveData.substring(moveData.length() - 1);

            if (pos >= 0 && pos < buttons.length) {
                buttons[pos].setText(value);
                buttons[pos].setEnabled(false); // Disable button after move
                isMyTurn = true; // Allow the other player to make a move
                enableButtons(); // Enable buttons for the other player
                if (checkForWin()) {
                    displayWinner("Player " + value + " wins!");
                } else if (checkForDraw()) {
                    displayWinner("The game is a draw!");
                }
            } else {
                System.out.println("Invalid move received: " + move);
            }
        } else if (decMessString.startsWith("CHAT:")) {
            chatArea.append(decMessString.substring(5) + "\n"); 
        }
    }

    private class SendButtonListener implements ActionListener {
       
        public void actionPerformed(ActionEvent e) {
            String message = chatInput.getText();
            if (!message.trim().isEmpty()) {
                chatArea.append("Me: " + message + "\n");
                String EncryptMessage = secure.EncryptMessage("CHAT:Player: "+message, key);
                try {
                    out.writeUTF(EncryptMessage);
                    out.flush();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                chatInput.setText("");
            }
        }
    }

    private void disableMyButtons() {
        for (JButton button : buttons) {
            if (button.getText().isEmpty()) {
                button.setEnabled(false);
            }
        }
    }

    private void enableButtons() {
        for (JButton button : buttons) {
            if (button.getText().isEmpty()) {
                button.setEnabled(true);
            }
        }
    }

    private boolean checkForWin() {
        // Check rows, columns, and diagonals
        return (checkRow(0) || checkRow(3) || checkRow(6) ||
                checkColumn(0) || checkColumn(1) || checkColumn(2) ||
                checkDiagonal());
    }

    private boolean checkRow(int start) {
        String first = buttons[start].getText();
        return !first.isEmpty() &&
               first.equals(buttons[start + 1].getText()) &&
               first.equals(buttons[start + 2].getText());
    }

    private boolean checkColumn(int start) {
        String first = buttons[start].getText();
        return !first.isEmpty() &&
               first.equals(buttons[start + 3].getText()) &&
               first.equals(buttons[start + 6].getText());
    }

    private boolean checkDiagonal() {
        String center = buttons[4].getText();
        return !center.isEmpty() &&
               ((center.equals(buttons[0].getText()) && center.equals(buttons[8].getText())) ||
                (center.equals(buttons[2].getText()) && center.equals(buttons[6].getText())));
    }

    private boolean checkForDraw() {
        for (JButton button : buttons) {
            if (button.getText().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void displayWinner(String message) {
        JOptionPane.showMessageDialog(null, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        f.dispose();
        new UIBuilder(key);
    }

    private class IncomingMessages implements Runnable {
       
        public void run() {
            try {
                DataInputStream in = new DataInputStream(s.getInputStream());
                currentPlayerMove = in.readUTF(); // Receive initial move value
                isFirstPlayer = "X".equals(currentPlayerMove);
                isMyTurn = isFirstPlayer; // Set the turn based on the initial move
                System.out.println("Initial move received: " + currentPlayerMove);

                while (true) {
                    String message = in.readUTF();
                    System.out.println("Message received: " + message);
                    getMove(message);
                }
            } catch (EOFException e) {
                System.err.println("Connection closed by server: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("IO Exception: " + e.getMessage());
            } finally {
                // Close the socket when done
                try {
                    s.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}

class RockPaperScissor {
    private Socket socket;
    private JTextArea chatArea;
    private JButton[] buttons;
    private JLabel scoreLabel;
    private String myMove = "";
    private int myScore = 0;
    private int opponentScore = 0;
    int key;
    JFrame f;
    JFrame frame = new JFrame("Rock Paper Scissors");

    public RockPaperScissor(JFrame f,int key) {
        this.f = f;
        f.dispose();
        try {
            socket = new Socket("localhost", 6668);
            new MessageListener(socket).start(); // Start the message listener thread
        } catch (Exception e) {
            SwingUtilities.invokeLater(()-> JOptionPane.showMessageDialog(f, "Cannot Connect to Server", "Connection Error", JOptionPane.ERROR_MESSAGE));
            // return
            // e.printStackTrace();
        }


        frame.setSize(1500, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Top Panel with Home Button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(1500, 50));
        topPanel.setBackground(new Color(0, 0, 51));

        JButton homeButton = new JButton("Home");
        homeButton.setPreferredSize(new Dimension(100, 30));
        homeButton.setFocusPainted(false);
        homeButton.setBackground(Color.RED);
        homeButton.setForeground(Color.WHITE);
        topPanel.add(homeButton, BorderLayout.WEST);

        JLabel selecJLabel = new JLabel("Click to Select");
        selecJLabel.setFont(new Font("Showcard Gothic", Font.BOLD, 40));
        selecJLabel.setForeground(Color.GREEN);
        selecJLabel.setHorizontalAlignment(JLabel.CENTER);
        selecJLabel.setBorder(new EmptyBorder(25, 0, 0, 0));
        topPanel.add(selecJLabel, BorderLayout.CENTER);

        frame.add(topPanel, BorderLayout.NORTH);

        // Game Board Panel
        JPanel gameBoard = new JPanel(new GridBagLayout());
        gameBoard.setPreferredSize(new Dimension(900, 900));
        gameBoard.setBackground(new Color(0, 0, 51));
        frame.add(gameBoard, BorderLayout.CENTER);

        // Score Panel
        JPanel scorePanel = new JPanel();
        scorePanel.setPreferredSize(new Dimension(1500, 50));
        scorePanel.setBackground(new Color(0, 0, 51));
        scoreLabel = new JLabel("Your Score:" +myScore+" | Opponent's Score: "+opponentScore);
        scoreLabel.setFont(new Font("Showcard Gothic", Font.BOLD, 30));
        scoreLabel.setForeground(Color.GREEN);
        scorePanel.add(scoreLabel);
        frame.add(scorePanel, BorderLayout.SOUTH);

        // Chat Panel
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(300, 900));
        chatPanel.setBackground(new Color(0, 0, 51));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(new Color(0, 0, 55));
        chatArea.setForeground(Color.WHITE);
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setPreferredSize(new Dimension(300, 50));
        inputPanel.setBackground(new Color(0, 0, 51));

        JTextField chatField = new JTextField();
        inputPanel.add(chatField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(100, 30));
        sendButton.setBackground(Color.GREEN);
        sendButton.setForeground(Color.WHITE);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        frame.add(chatPanel, BorderLayout.EAST);

        // Button Settings
        Color[] colors = { Color.YELLOW, Color.GREEN, Color.ORANGE };
        String[] img = { "./assets/rock.png", "./assets/paper.png", "./assets/scissors.png" };
        String[] btnNames = { "Rock", "Paper", "Scissors" };
        buttons = new JButton[3];
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;

        for (int i = 0; i < buttons.length; i++) {
            ImageIcon icon = new ImageIcon(img[i]);
            buttons[i] = new JButton(icon);
            buttons[i].setFocusPainted(false);
            buttons[i].setBackground(colors[i]);
            buttons[i].setPreferredSize(new Dimension(200, 200));
            buttons[i].setBorderPainted(false);
            gameBoard.add(buttons[i], gbc);
            gbc.gridx++;

            buttons[i].addActionListener(new ButtonClickListener(selecJLabel, btnNames[i]));
        }

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String message = chatField.getText().trim();
                if (!message.isEmpty()) {
                    chatArea.append("You: " + message + "\n");
                    sendMessageToServer(message); // Send message to the server
                    chatField.setText("");
                }
            }
        });

        homeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                frame.dispose();
                new UIBuilder(key);
            }
        });

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private class ButtonClickListener implements ActionListener {
        private JLabel label;
        private String buttonName;

        public ButtonClickListener(JLabel label, String buttonName) {
            this.label = label;
            this.buttonName = buttonName;
        }

        public void actionPerformed(ActionEvent e) {
            myMove = buttonName;
            label.setText("You Selected: " + buttonName);
            chatArea.append("You Selected: " + buttonName + "\n");

            sendMessageToServer(buttonName);

            for (JButton button : buttons) {
                button.setEnabled(false);
            }
        }
    }

    private void sendMessageToServer(String message) {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MessageListener extends Thread {
        private Socket socket;
    
        public MessageListener(Socket socket) {
            this.socket = socket;
        }
    
        public void run() {
            try (DataInputStream in = new DataInputStream(socket.getInputStream())) {
                while (true) {
                    String message = in.readUTF();
                    SwingUtilities.invokeLater(() -> {
                        if (message.startsWith("Result:")) {
                            // Example message: "Result: You lose. Player 2 selected Rock"
                            chatArea.append(message + "\n");
    
                            // Extract the result and opponent's move
                            String resultMessage = message.substring(7); // Remove "Result: " from the start
                            JOptionPane.showMessageDialog(null, resultMessage, "Game Result", JOptionPane.INFORMATION_MESSAGE);
    
                            // Re-enable buttons for a new game
                            for (JButton button : buttons) {
                                button.setEnabled(true);
                            }
                        } else if (message.startsWith("Score:")) {
                            // Update score display
                            String[] scores = message.substring(6).split(";");
                            myScore = Integer.parseInt(scores[0]);
                            opponentScore = Integer.parseInt(scores[1]);
                            // scoreLabel.setText("Your Score: " + myScore + " | Opponent's Score: " + opponentScore);
                        } else {
                            chatArea.append(message + "\n");
                            JOptionPane.showMessageDialog(frame, message, "Game Result", JOptionPane.INFORMATION_MESSAGE);
                            if(message.contains("You win!")){
                                myScore++;
                                scoreLabel.setText("Your Score:" +myScore+" | Opponent's Score: "+opponentScore);
                            }
                             if(message.contains("You lose!")){
                                opponentScore++;
                                scoreLabel.setText("Your Score:" +myScore+" | Opponent's Score: "+opponentScore);
                            }

                            if(myScore >= 5 && opponentScore < 5){
                                JOptionPane.showMessageDialog(frame, "You Won!!!", "Game Ended", JOptionPane.INFORMATION_MESSAGE);
                                return;
                            } if(opponentScore >= 5){
                                JOptionPane.showMessageDialog(frame, "You Lose!!!", "Game Ended", JOptionPane.INFORMATION_MESSAGE);
                                return;
                            }
                            for(int i = 0 ; i < buttons.length;i++){
                                buttons[i].setEnabled(true);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }   
}
