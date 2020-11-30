package server;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    private final Socket socket;
    private final DataInputStream dataInputStream;
    private final DataOutputStream dataOutputStream;
    private final Server server;

    public ServerThread(final Socket socket, final Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());

    }

    public void notify(byte[] message) throws IOException {
        dataOutputStream.write(message);
        dataOutputStream.flush();
    }

    @Override
    public void run() {
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
                    server.notifyClients(temp.toByteArray());
                    temp.reset();
                }

                sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            //pass to finally
        } finally {
            server.unregisterClient(this);
            try {
                dataInputStream.close();
                dataOutputStream.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
