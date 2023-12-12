package connection.multiplexer;

import concurrentUtils.ConcurrentCounter;
import connection.utils.Message;

import java.io.IOException;
import java.net.Socket;

public class MultiplexedConnection implements Runnable, AutoCloseable {
    private final TaggedConnection connection;
    private final MessageMultiplexer messageMultiplexer = new MessageMultiplexer();
    private final ConcurrentCounter nextTag = new ConcurrentCounter();

    public MultiplexedConnection(TaggedConnection connection) {
        this.connection = connection;
    }

    public MultiplexedConnection(Socket socket) throws IOException {
        this(new TaggedConnection(socket));
    }

    public void run() {
        while (true) {
            try {
                messageMultiplexer.put(connection.receive());
            } catch (IOException e) {
                messageMultiplexer.killUntil(nextTag.get());
            }
        }
    }

    public int send(Message message) throws IOException {
        int tag = nextTag.next();
        connection.send(tag, message);
        return tag;
    }

    public Message receive(int tag) throws IOException, InterruptedException {
        return messageMultiplexer.get(tag);
    }

    public void close() throws IOException {
        connection.close();
    }
}
