package agprogramlama.ag;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientApp extends JFrame {
    private JTextArea messageArea;
    private JTextArea logArea;
    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JTextField messageField;
    private JButton sendButton;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Timer timer;

    public ClientApp() {
        
        setTitle("Client");
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

        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        JLabel ipLabel = new JLabel("IP Adresi:");
        ipField = new JTextField();
        JLabel portLabel = new JLabel("Port Numarası:");
        portField = new JTextField();

        inputPanel.add(ipLabel);
        inputPanel.add(ipField);
        inputPanel.add(portLabel);
        inputPanel.add(portField);

        connectButton = new JButton("Bağlan");
        connectButton.setBackground(new Color(55, 188, 97));
        JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        connectPanel.add(connectButton);

        bottomPanel.add(inputPanel, BorderLayout.NORTH);
        bottomPanel.add(connectPanel, BorderLayout.CENTER);

        messageField = new JTextField();
        sendButton = new JButton("Gönder");
        sendButton.setBackground(new Color(55, 188, 97));
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);
        bottomPanel.add(messagePanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Istemci_baglan();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                istemci_mesaj_gonder();
            }
        });

        // WindowListener ekleniyor
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (out != null) {
                    out.println("Client Bağlantısı Kesildi"); 
                    out.flush(); 
                }
                Istemci_dur(); 
            }
        });
    }

    private void Istemci_baglan() {
        String ip = ipField.getText();
        String portText = portField.getText();

        if (ip.isEmpty() || portText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lütfen IP adresi ve port numarası girin!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int port = Integer.parseInt(portText);
            socket = new Socket(ip, port);
            bilgi("Connected to server: " + ip + ":" + port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Sunucudan gelen mesajları dinler
            timer = new Timer(100, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (in.ready()) {
                            String inputLine = in.readLine();
                            if (inputLine != null) {
                                messageArea.append("Server (" + socket.getInetAddress() + "): " + inputLine + " [" + zaman() + "]\n");
                            }
                        }
                    } catch (IOException ex) {
                        bilgi("Sunucuya Bağlanılamadı: " + ip + ":" + port + " [" + zaman() + "]");
                        Istemci_dur(); 
                    }
                }
            });
            timer.start();
        } 
         catch (IOException e) {
            bilgi("Sunucuya Bağlanma Hatası: " + e.getMessage());
        }
    }

    private void Istemci_dur() {
        try {
            if (timer != null) timer.stop();
            if (socket != null) socket.close();
            bilgi("İstemci Durdu.");
        } catch (IOException e) {
            bilgi("İstemci durma hatası: " + e.getMessage());
        }
    }

    private void istemci_mesaj_gonder() {
        String mesaj = messageField.getText();
        if (mesaj != null && !mesaj.isEmpty()) {
            out.println(mesaj);
            messageArea.append("İstemci (" + socket.getLocalAddress() + "): " + mesaj + " [" + zaman() + "]\n");
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
                new ClientApp().setVisible(true);
            }
        });
    }
}