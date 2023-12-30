package server;

import connectionUtils.MultiplexedConnection;
import connectionUtils.TaggedConnection;
import server.WorkerLink;
import server.WorkerPool;

import java.io.IOException;

public class WorkerMultiplexedConnection extends MultiplexedConnection {
    private final WorkerPool workerPool;
    private final WorkerLink workerLink;

    public WorkerMultiplexedConnection(TaggedConnection connection, WorkerPool workerPool, WorkerLink workerLink) {
        super(connection);
        this.workerPool = workerPool;
        this.workerLink = workerLink;
    }

    @Override
    public void run() {
        while (true) {
            try {
                messageMultiplexer.put(connection.receive());
            } catch (IOException e) {
                System.out.println("Worker disconnected");
                messageMultiplexer.kill(e);
                workerPool.remove(workerLink);
                break;
            }
        }
    }
}
