package connectionUtils;

import concurrentUtils.ConcurrentCounter;
import messages.utils.Message;

import java.io.IOException;
import java.net.Socket;

public class MultiplexedConnection implements Runnable, AutoCloseable {
    protected final TaggedConnection connection;
    protected final MessageMultiplexer messageMultiplexer = new MessageMultiplexer();
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
            } catch (IOException ioe) {
                messageMultiplexer.kill(ioe);
                break;
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

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
