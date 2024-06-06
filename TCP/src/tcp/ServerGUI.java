package tcp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import tcp.ServerThread;

public class ServerGUI extends JFrame {

    private JTextArea logArea;
    private JButton startButton;
    private JButton stopButton;
    private JTextField ipField;
    private JTextField portField;
    private ServerThread serverThread;

    public ServerGUI() {
        setTitle("TCP Chat Messages Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 300);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        ipField = new JTextField("localhost", 10);
        portField = new JTextField("1234", 5);

        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        stopButton.setEnabled(false);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        controlPanel.add(new JLabel("IP:"));
        controlPanel.add(ipField);
        controlPanel.add(new JLabel("Port:"));
        controlPanel.add(portField);
        controlPanel.add(startButton);
        controlPanel.add(stopButton);

        panel.add(controlPanel,
                 BorderLayout.NORTH);
        add(panel);

        setVisible(true);
    }

    private void startServer() {
        String ip = ipField.getText().trim();
        String portStr = portField.getText().trim();
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            logMessage("Invalid port number.");
            return;
        }

        serverThread = new ServerThread(this, ip, port);
        serverThread.start();
        logMessage("Server is starting...");
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    private void stopServer() {
        if (serverThread != null) {
            serverThread.stopServer();
            logMessage("Server stopped.");
        }
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }
}
