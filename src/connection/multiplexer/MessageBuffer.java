package connection.multiplexer;

import connection.utils.Message;

import java.io.IOException;
import java.util.concurrent.locks.Condition;

public class MessageBuffer {
    private final Condition connection;
    private Message messageBuffer = null;
    private IOException ioException = null;

    public MessageBuffer(Condition connection) {
        this.connection = connection;
    }

    public void put(Message message) {
        messageBuffer = message;
        connection.signal();
    }

    public Message get() throws IOException, InterruptedException {
        while (messageBuffer == null) {
            connection.await();
            if (ioException != null) {
                throw ioException;
            }
        }

        return messageBuffer;
    }

    public void kill(IOException ioe) {
        this.ioException = ioe;
        connection.signal();
    }
}
