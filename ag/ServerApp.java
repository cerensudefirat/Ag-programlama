package agprogramlama.ag;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerApp extends JFrame {
    private JTextArea messageArea;
    private JTextArea logArea;
    private JTextField portField;
    private JButton startButton;
    private JTextField messageField;
    private JButton sendButton;
    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Timer timer;

    public ServerApp() {
        setTitle("Server");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        logArea = new JTextArea(5, 20);
        logArea.setEditable(false);
        bottomPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JLabel portLabel = new JLabel("Port Numarası:");
        portField = new JTextField(10);
        startButton = new JButton("Bağlan");
        startButton.setBackground(new Color(55, 188, 97));

        JPanel portPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        portPanel.add(portLabel);
        portPanel.add(portField);
        inputPanel.add(portPanel, BorderLayout.WEST);
        inputPanel.add(startButton, BorderLayout.EAST);

        bottomPanel.add(inputPanel, BorderLayout.NORTH);

        messageField = new JTextField();
        sendButton = new JButton("Gönder");
        sendButton.setBackground(new Color(55, 188, 97));
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.add(messagePanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Server_Baglan();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Sunucu_mesaj_gonder();
            }
        });
    }

    private void Server_Baglan() {
       String portText = portField.getText();
    if (portText.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Lütfen bir port numarası girin!", "Hata", JOptionPane.ERROR_MESSAGE);
        return;
    }

    try {
        int port = Integer.parseInt(portText);
        serverSocket = new ServerSocket(port);
        bilgi("Sunucu bağlanma portu: " + port);
        
        socket = serverSocket.accept();
        bilgi("İstemci Bağlandı: " + socket.getInetAddress() + ":" + socket.getPort()); 

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        timer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (in.ready()) {
                        String inputLine = in.readLine();
                        if (inputLine != null) {
                            if (inputLine.equals("İstemci bağlanamadı")) {
                                bilgi("İstemci Bağlanamadı: "+ socket.getInetAddress() + " [" + zaman() + "]");
                                SunucuDur();
                            } else {
                                messageArea.append("Client (" + socket.getInetAddress() + "): " + inputLine + " [" + zaman() + "]\n");
                            }
                        }
                    }
                } catch (IOException ex) {
                    bilgi("Client bağlanamadı: " + socket.getInetAddress() + " [" + zaman() + "]");
                    SunucuDur();
                }
            }
        });
        timer.start();
    } catch (IOException e) {
        bilgi("Sunucu başlatma hatası: " + e.getMessage());
    }
    }

    private void SunucuDur() {
        try {
            if (timer != null) timer.stop();
            if (socket != null) socket.close();
            if (serverSocket != null) serverSocket.close();
            bilgi("Server Durdu.");
        } catch (IOException e) {
            bilgi("Server durma hatası:  " + e.getMessage());
        }
    }

    private void Sunucu_mesaj_gonder() {
        String message = messageField.getText();
        if (message != null && !message.isEmpty()) {
            out.println(message);
            
            messageArea.append("Sunucu (" + serverSocket.getInetAddress().getHostAddress() + "): " + message + " [" + zaman() + "]\n");
            messageField.setText("");
        }
    }

    private void bilgi(String message) {
        logArea.append(message + " [" + zaman() + "]\n");
    }

    private String zaman() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ServerApp().setVisible(true);
            }
        });
    }
}