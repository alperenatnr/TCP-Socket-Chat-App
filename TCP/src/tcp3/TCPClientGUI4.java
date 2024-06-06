package tcp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class TCPClientGUI4 extends JFrame {

    private JTextField ipField;
    private JTextField portField;
    private JTextField usernameField;
    private JTextArea messageArea;
    private JTextField messageField;
    private JButton connectButton;
    private JButton sendButton;
    private PrintWriter out;
    private Scanner input;
    private Socket socket;

    public TCPClientGUI4() {
        setTitle("TCP Chat Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(500, 400);

        ipField = new JTextField("localhost", 10);
        portField = new JTextField("1234", 5);
        usernameField = new JTextField(10);
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        messageField = new JTextField(30);
        connectButton = new JButton("Bağlan");
        sendButton = new JButton("Gönder");
        sendButton.setEnabled(false);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new FlowLayout());
        northPanel.add(new JLabel("IP:"));
        northPanel.add(ipField);
        northPanel.add(new JLabel("Port:"));
        northPanel.add(portField);
        northPanel.add(new JLabel("Kullanıcı Adı:"));
        northPanel.add(usernameField);
        northPanel.add(connectButton);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout());
        southPanel.add(messageField);
        southPanel.add(sendButton);

        setLayout(new BorderLayout());
        add(northPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        setVisible(true);
    }

    private void connectToServer() {
        String ip = ipField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            showMessage("Geçersiz port numarası.");
            return;
        }

        String username = usernameField.getText().trim();
        if (username.isEmpty() || username.length() < 5) {
            showMessage("Kullanıcı adı boş veya 5 harften küçük olamaz.");
            return;

        }

        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            input = new Scanner(socket.getInputStream());
            out.println(username);  // Kullanıcı adını sunucuya gönder
            showMessage(username + " katıldı.");

            new Thread(() -> listenForMessages()).start();

            connectButton.setEnabled(false);
            ipField.setEnabled(false);
            portField.setEnabled(false);
            usernameField.setEnabled(false);
            sendButton.setEnabled(true);
        } catch (IOException ex) {
            showMessage("Sunucuya bağlanılamadı. Hata mesajı: " + ex.getMessage());
        }
    }

    private void listenForMessages() {
        while (input.hasNextLine()) {
            String message = input.nextLine();
            showMessage(message);
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        String username = usernameField.getText().trim();
        String fullMessage = username + " > " + message;
        out.println(fullMessage);
        messageField.setText("");
        if ("exit".equalsIgnoreCase(message)) {
            closeConnection();
        }
    }

    private void showMessage(String message) {
        SwingUtilities.invokeLater(() -> messageArea.append(message + "\n"));
    }

    private void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ex) {
            showMessage("Bağlantı kapatılamadı! Hata mesajı: " + ex.getMessage());
        } finally {
            connectButton.setEnabled(true);
            ipField.setEnabled(true);
            portField.setEnabled(true);
            usernameField.setEnabled(true);
            sendButton.setEnabled(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TCPClientGUI4::new);
    }
}
