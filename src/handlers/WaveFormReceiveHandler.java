package handlers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class WaveFormReceiveHandler extends ReceiveHandler {

    public WaveFormReceiveHandler() {
        this.dataType = 2;
        this.receiveHandler = null;
    }

    @Override
    protected Object handle(byte[] message) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(Arrays.copyOfRange(message, 1, message.length));
        File file = new File("temp/message.wav");
        Files.copy(bis, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return file;
    }
}
