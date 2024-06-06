package tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ServerThread extends Thread {

    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private ServerGUI gui;
    private String ip;
    private int port;
    private volatile boolean running;

    public ServerThread(ServerGUI gui, String ip, int port) {
        this.gui = gui;
        this.ip = ip;
        this.port = port;
        this.clients = new ArrayList<>();
        this.running = true;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));
            gui.logMessage("Sunucu TCP Socket oluşturuldu. Bağlantı bekleniyor...");

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    gui.logMessage(clientSocket.toString() + " bağlandı.");

                    ClientHandler handler = new ClientHandler(clientSocket, clients, gui);
                    clients.add(handler);
                    handler.start();
                } catch (IOException e) {
                    if (running) {
                        gui.logMessage("Bağlantı hatası: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            gui.logMessage("Sunucu başlatılamadı: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        running = false;
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                gui.logMessage("Sunucu kapatılamadı: " + e.getMessage());
            }
        }
        for (ClientHandler client : clients) {
            client.closeConnection();
        }
    }
}




class ClientHandler extends Thread {

    private Socket clientSocket;
    private List<ClientHandler> clients;
    private PrintWriter out;
    private Scanner input;
    private ServerGUI gui;
    private String username;

    public ClientHandler(Socket socket, List<ClientHandler> clients, ServerGUI gui) {
        this.clientSocket = socket;
        this.clients = clients;
        this.gui = gui;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            input = new Scanner(clientSocket.getInputStream());
        } catch (IOException ex) {
            gui.logMessage("Bağlantı hatası: " + ex.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            username = input.nextLine();
            broadcastMessage(username + " has joined");

            String gelenVeri;
            do {
                gelenVeri = input.nextLine();
                String[] parcalar = gelenVeri.split(">", 2);
                if (parcalar.length == 2) {
                    String isim = parcalar[0].trim();
                    String mesaj = parcalar[1].trim();
                    gui.logMessage(isim + ": " + mesaj);

                    // Mesajı diğer istemcilere gönder
                    broadcastMessage(gelenVeri);

                    // Mesajı gönderen istemciye de gönder (Uppercase)
                    out.println(gelenVeri.toUpperCase());
                } else {
                    gui.logMessage("Geçersiz mesaj formatı: " + gelenVeri);
                }
            } while (!gelenVeri.equalsIgnoreCase("exit"));
        } catch (Exception ex) {
            gui.logMessage("Bağlantı hatası: " + ex.getMessage());
        } finally {
            closeConnection();
            broadcastMessage(username + " has left");
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            if (client != this) {
                client.sendMessage(message);
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void closeConnection() {
        if (clientSocket != null) {
            try {
                gui.logMessage("Bağlantı kapatılıyor: " + clientSocket);
                clientSocket.close();
                clients.remove(this);
            } catch (IOException ex) {
                gui.logMessage("Bağlantı kapatılamadı: " + ex.getMessage());
            }
        }
    }
}