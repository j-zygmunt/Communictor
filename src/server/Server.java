package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private ArrayList<ServerThread> clients = new ArrayList<>();
    private final int port;
    private ServerSocket serverSocket;

    public Server(final int port) {
        this.port = port;
    }

    public void launchServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            while (true) {
                registerClient(serverSocket);
            }

        } catch (IOException e) {
            try {
                serverSocket.close();

                for (ServerThread thread : clients) {
                    unregisterClient(thread);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void registerClient(final ServerSocket serverSocket) throws IOException {
        Socket socket = serverSocket.accept();
        ServerThread serverThread = new ServerThread(socket, this);
        clients.add(serverThread);
        System.out.println("new client");
        System.out.println("number of clients:" + clients.size());
        serverThread.start();
    }

    public void unregisterClient(ServerThread client) {
        clients.remove(client);
        System.out.println("client logged");
        System.out.println("number of clients:" + clients.size());
    }

    public void notifyClients(byte[] message) throws IOException {
        for (ServerThread client : clients) {
            client.notify(message);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter server port");
        String port = reader.readLine();
        Server server = new Server(Integer.parseInt(port));
        server.launchServer();
    }
}