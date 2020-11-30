package handlers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SendHandler {

    public static void handleRequest(byte[] content, String dataType, DataOutputStream dos) throws IOException {
        ByteArrayOutputStream temp = new ByteArrayOutputStream();

        if(dataType.equals("0")){
            temp.write("0".getBytes());
        }

        if (dataType.equals("bmp")) {
            temp.write("1".getBytes());
        }

        if (dataType.equals("wav")) {
            temp.write("2".getBytes());
        }

        temp.write(content);
        byte[] message = temp.toByteArray();
        dos.write(message);
        dos.flush();
    }
}
