package connection.multiplexer;

import connection.utils.Message;

import java.io.IOException;
import java.util.concurrent.locks.Condition;

public class MessageBuffer {
    private final Condition connection;
    private Message messageBuffer = null;
    private boolean error = false;

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
            if (error) {
                throw new IOException();
            }
        }

        return messageBuffer;
    }

    public void kill() {
        error = true;
        connection.signal();
    }
}
