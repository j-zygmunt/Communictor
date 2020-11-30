package handlers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class BitmapReceiveHandler extends ReceiveHandler {

    public BitmapReceiveHandler() {
        this.dataType = 1;
        this.receiveHandler = new WaveFormReceiveHandler();
    }

    @Override
    protected Object handle(byte[] message) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(Arrays.copyOfRange(message, 1, message.length));
        File file = new File("message.bmp");
        Files.copy(bis, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return file;
    }
}
