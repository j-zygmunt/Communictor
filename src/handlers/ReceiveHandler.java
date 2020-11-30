package handlers;

import java.io.IOException;

public abstract class ReceiveHandler {

    protected int dataType;
    protected ReceiveHandler receiveHandler;

    public Object handleRequest(byte[] message) throws IOException {
        int type = getDataType(message);
        if (!checkDataType(type)) return receiveHandler.handleRequest(message);
        else return handle(message);
    }

    private int getDataType(byte[] message) {
        return Character.getNumericValue(message[0]);
    }

    private boolean checkDataType(int type) {
        return this.dataType == type;
    }

    abstract protected Object handle(byte[] message) throws IOException;
}
