package Player.Games;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

import Player.Security.*;

public class TicTaToeGameConn {
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

    public TicTaToeGameConn(int tkey) {
        this.key =tkey ;

        try {
            this.s = new Socket("localhost", 6667); 
            System.out.println("TicTacToeGame key: "+key);
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

        //Home button
        JButton homeButton = new JButton();
        homeButton = createStyledButton("Home", Color.RED);
        // Create top panel
        JPanel topPanel = new JPanel();
        topPanel.setPreferredSize(new Dimension(100, 50));
        topPanel.setBackground(new Color(0, 0, 51));

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
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
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

        @Override
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
                    displayWinner("Player " + currentPlayerMove + " wins!",f);
                } else if (checkForDraw()) {
                    displayWinner("The game is a draw!",f);
                }
            } else {
                System.out.println("Invalid move attempt by player: " + currentPlayerMove);
            }
        }
    }

    private void setMove(int index) {
        try {
            if (out != null) {
                out.writeUTF("MOVE:" + index + currentPlayerMove);
                out.flush(); // Ensure data is sent immediately
            }
        } catch (IOException e) {
            showErrorMessage("Error sending move: " + e.getMessage());
        }
    }

    private void getMove(String move) {
        SwingUtilities.invokeLater(() -> {
            // Decrypt the received move
            String deMove = secure.DecryptMessage(move, key); 
            System.out.println("Decrypted Message: " + deMove);
    
            // Check if it's a move
            if (deMove.startsWith("MOVE:")) {
                String moveData = deMove.substring(5); // Remove "MOVE:" prefix
                try {
                    int pos = Integer.parseInt(moveData.substring(0, moveData.length() - 1)); // Get position
                    String value = moveData.substring(moveData.length() - 1); // Get X or O
    
                    if (pos >= 0 && pos < buttons.length) {
                        buttons[pos].setText(value); // Set X or O
                        buttons[pos].setEnabled(false); // Disable button after move
                        isMyTurn = true; // Allow the other player to make a move
                        enableButtons(); // Enable buttons for the other player
    
                        // Check for a win or draw
                        if (checkForWin()) {
                            displayWinner("Player " + value + " wins!", f);
                        } else if (checkForDraw()) {
                            displayWinner("The game is a draw!", f);
                        }
                    } else {
                        System.out.println("Invalid move received: " + deMove);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error parsing move: " + e.getMessage());
                }
            } else if (deMove.startsWith("CHAT:")) {
                chatArea.append(deMove.substring(5) + "\n"); // Remove "CHAT:" prefix and display in chat area
            } else {
                System.out.println("Unrecognized message format: " + deMove);
            }
        });
    }
    

    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = chatInput.getText();
    
            if (!message.trim().isEmpty()) {
                chatArea.append("Me: " + message + "\n");
                String encmessage = secure.EncryptMessage("CHAT:Player: "+message, key); // Encrypting message
                System.out.println("Move received from player " + currentPlayerMove + ":Enc Message" + encmessage);
                
                try {
                    if (out != null) {
                        out.writeUTF(encmessage); // Sending encrypted message to server
                        out.flush();
                    }
                } catch (IOException ioException) {
                    showErrorMessage("Error sending chat message: " + ioException.getMessage());
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

    private void displayWinner(String message,JFrame f) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
            f.dispose();
        });
    }

    private class IncomingMessages implements Runnable {
        @Override
        public void run() {
            try {
                DataInputStream in = new DataInputStream(s.getInputStream());
                currentPlayerMove = in.readUTF(); // Receive initial move value
                isFirstPlayer = "X".equals(currentPlayerMove);
                isMyTurn = isFirstPlayer; // Set the turn based on the initial move
                System.out.println("Initial move received: " + currentPlayerMove);

                while (true) {
                    String message = in.readUTF();
                    getMove(message);
                }
            } catch (IOException e) {
                showErrorMessage("Connection lost: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
