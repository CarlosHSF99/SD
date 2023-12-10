package server;

import concurrentUtils.BoundedBuffer;
import connection.utils.Connection;
import connection.utils.Message;

import java.io.IOException;

public class ClientReader implements Runnable {
    private final Connection connection;
    private final BoundedBuffer<Message> replyBuffer;

    public ClientReader(Connection connection, BoundedBuffer<Message> replyBuffer) {
        this.connection = connection;
        this.replyBuffer = replyBuffer;
    }

    @Override
    public void run() {
        while (true) {
            try {
                replyBuffer.put(connection.receive());
            } catch (IOException | InterruptedException e) {
                var exceptionMessage = e.getMessage();
                System.out.println("Error processing request" + (exceptionMessage != null ? ": " + exceptionMessage : "."));
                break;
            }
        }
    }
}
