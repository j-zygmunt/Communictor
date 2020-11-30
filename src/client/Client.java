package client;

import handlers.ReceiveHandler;
import handlers.SendHandler;
import handlers.TextReceiveHandler;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import static java.lang.Thread.sleep;

public class Client extends JFrame {
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String userName;

    private JButton connectButton = new JButton();
    private JTextField addressTextField = new JTextField();
    private JTextField userNameTextField = new JTextField();
    private JTextField portTextField = new JTextField();
    private JDialog connectionDialog = new JDialog();
    private JTextArea chatTextArea = new JTextArea();
    private JTextArea messageTextField = new JTextArea();

    public Client() {
        JPanel mainPanel = new JPanel();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(500, 600);
        this.setResizable(false);
        this.setTitle("Chat");
        setWindowCenter(this);
        mainPanel.setBackground(Color.darkGray);
        mainPanel.setLayout(null);

        JButton fileButton = new JButton();
        fileButton.setText("choose file");
        fileButton.setOpaque(true);
        fileButton.setForeground(Color.black);
        fileButton.setBackground(Color.gray);
        fileButton.addActionListener(e -> fileButtonAction());
        fileButton.setBounds(360, 450, 100, 20);

        JButton sendButton = new JButton();
        sendButton.setText("send");
        sendButton.setOpaque(true);
        sendButton.setForeground(Color.black);
        sendButton.setBackground(Color.gray);
        sendButton.addActionListener(e -> {
            int count = messageTextField.getText().replaceAll(" ", "").length();
            if (count == 0) {
                messageTextField.setText("");
                return;
            }
            byte[] content = ("[" + userName + "]: " + messageTextField.getText()).getBytes();
            messageTextField.setText("");
            try {
                SendHandler.handleRequest(content, "0", dataOutputStream);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        sendButton.setBounds(360, 480, 100, 20);

        messageTextField.setLineWrap(true);
        messageTextField.setVisible(true);
        messageTextField.setBackground(Color.gray);

        JScrollPane messagePanel = new JScrollPane(messageTextField);
        messagePanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        messagePanel.setBounds(20, 450, 330, 80);

        JScrollPane chatPanel = new JScrollPane(chatTextArea);
        chatPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatPanel.setBounds(20, 20, 440, 400);

        chatTextArea.setEditable(false);
        chatTextArea.setLineWrap(true);
        chatTextArea.setVisible(true);
        chatTextArea.setBackground(Color.gray);

        JButton clearButton = new JButton("clear");
        clearButton.setOpaque(true);
        clearButton.setForeground(Color.black);
        clearButton.setBackground(Color.gray);
        clearButton.addActionListener(e -> {
            chatTextArea.setText("");
            chatTextArea.setCaretPosition(chatTextArea.getDocument().getLength());
        });
        clearButton.setBounds(360, 510, 100, 20);

        mainPanel.add(chatPanel);
        mainPanel.add(fileButton);
        mainPanel.add(sendButton);
        mainPanel.add(clearButton);
        mainPanel.add(messagePanel);
        this.add(mainPanel);
        showConnectionDialog();
    }

    private void setWindowCenter(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);
    }

    private void showConnectionDialog() {

        JPanel connectionPanel = new JPanel();
        connectionDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JLabel addressLabel = new JLabel("address:    ");
        JLabel portLabel = new JLabel("port:           ");
        JLabel userNameLabel = new JLabel("username:");

        addressTextField.setColumns(10);
        portTextField.setColumns(10);
        userNameTextField.setColumns(10);

        connectionDialog.setTitle("chat connection...");
        connectionDialog.setSize(200, 150);
        connectionDialog.setResizable(false);

        connectButton.setText("connect");
        connectButton.setOpaque(true);
        connectButton.setForeground(Color.black);
        connectButton.setBackground(Color.gray);
        connectButton.addActionListener(e -> connectButtonAction());

        connectionPanel.setBackground(Color.lightGray);
        connectionPanel.add(addressLabel);
        connectionPanel.add(addressTextField);
        connectionPanel.add(portLabel);
        connectionPanel.add(portTextField);
        connectionPanel.add(userNameLabel);
        connectionPanel.add(userNameTextField);
        connectionPanel.add(connectButton);

        setWindowCenter(connectionDialog);
        connectionDialog.add(connectionPanel);
        connectionDialog.setVisible(true);
    }

    private void connectButtonAction() {
        String address = addressTextField.getText();
        int port = Integer.parseInt(portTextField.getText());

        if (userNameTextField.getText().isBlank())
            JOptionPane.showMessageDialog(connectionDialog, "Username field cannot be empty", "Connection Error", 1);

        else {
            userName = userNameTextField.getText();
            try {
                socket = new Socket(address, port);
                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                this.setVisible(true);
                connectionDialog.setVisible(false);
                new Thread(() -> {
                    ByteArrayOutputStream temp = new ByteArrayOutputStream();
                    byte[] start = new byte[1];
                    try {
                        while (!socket.isClosed()) {
                            dataInputStream.read(start);
                            while (dataInputStream.available() > 0) {
                                temp.write(start);
                                int size = dataInputStream.available();
                                byte[] buf = new byte[size];
                                dataInputStream.read(buf);
                                temp.write(buf);
                                sleep(100);
                                if (dataInputStream.available() > 0) continue;
                                ReceiveHandler receiveHandler = new TextReceiveHandler();
                                var o = receiveHandler.handleRequest(temp.toByteArray());
                                temp.reset();
                                if (o instanceof String) {
                                    chatTextArea.append(o + "\n");
                                    chatTextArea.setCaretPosition(chatTextArea.getDocument().getLength());
                                } else {
                                    Desktop.getDesktop().open((File) o);
                                }
                            }
                            sleep(100);
                        }
                    } catch (IOException | InterruptedException e) {
                        int res = JOptionPane.showConfirmDialog(this, "An error occurred while receiving the message", "Error", JOptionPane.OK_OPTION);
                        if (res == JOptionPane.OK_OPTION) System.exit(-1);
                    } finally {
                        try {
                            dataInputStream.close();
                            dataOutputStream.close();
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(connectionDialog, "Check if you typed address and port correctly", "Connection Error", 1);
            }
        }
    }

    private void fileButtonAction() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter bmpFilter = new FileNameExtensionFilter("BMP file", "bmp");
        FileNameExtensionFilter wavFilter = new FileNameExtensionFilter("WAV file", "wav");
        fileChooser.addChoosableFileFilter(bmpFilter);
        fileChooser.addChoosableFileFilter(wavFilter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        String fileExtension = "";
        int ret = fileChooser.showOpenDialog(this);

        if (ret == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            int extensionIndex = selectedFile.getName().lastIndexOf(".");

            if (extensionIndex > 0) {
                fileExtension = selectedFile.getName().substring(extensionIndex + 1);
            }

            try {
                byte[] message = ("[" + userName + "]: sent " + fileExtension + "file").getBytes();
                SendHandler.handleRequest(message, "0", dataOutputStream);
                sleep(1000);
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                SendHandler.handleRequest(fileContent, fileExtension, dataOutputStream);
            } catch (IOException | InterruptedException e) {
                int res = JOptionPane.showConfirmDialog(this, "An error occurred while sending message", "Error", JOptionPane.OK_OPTION);
                if (res == JOptionPane.OK_OPTION) System.exit(-1);
            }

        }
    }

    public static void main(String[] args) {
        Client client = new Client();
    }
}
