package Xo.Rock;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

class Main {
    public static void main(String[] args) throws IOException {
        new RockPaperScissor();
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
    JFrame frame = new JFrame("Rock Paper Scissors");

    public RockPaperScissor() {
        try {
            socket = new Socket("localhost", 6668);
            new MessageListener(socket).start(); // Start the message listener thread
        } catch (Exception e) {
            e.printStackTrace();
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
            buttons[i] = new JButton(btnNames[i]);
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

        frame.setVisible(true);
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
