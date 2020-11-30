package handlers;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TextReceiveHandler extends ReceiveHandler {

    public TextReceiveHandler() {
        this.dataType = 0;
        this.receiveHandler = new BitmapReceiveHandler();
    }

    @Override
    protected Object handle(byte[] message) {
        return new String(Arrays.copyOfRange(message, 1, message.length), StandardCharsets.UTF_8);
    }
}
