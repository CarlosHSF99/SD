package server;

import connection.messages.JobRequest;
import connection.multiplexer.MultiplexedConnection;
import connection.multiplexer.TaggedConnection;
import connection.utils.Message;

import java.io.IOException;

public class WorkerLink implements Runnable, AutoCloseable, Comparable<WorkerLink> {
    private final MultiplexedConnection connection;
    private final int totalMemory;
    private int memoryInUse = 0;

    public WorkerLink(TaggedConnection connection, int totalMemory) {
        this.connection = new MultiplexedConnection(connection);
        this.totalMemory = totalMemory;
    }

    @Override
    public void run() {
        new Thread(connection).start();
    }

    public Message runJob(JobRequest jobRequest) throws IOException, InterruptedException {
        return connection.receive(connection.send(jobRequest));
    }

    public void allocMemory(int memory) {
        memoryInUse += memory;
    }

    public void freeMemory(int memory) {
        memoryInUse -= memory;
    }

    public int totalMemory() {
        return totalMemory;
    }

    public int availableMemory() {
        return totalMemory - memoryInUse;
    }

    @Override
    public int compareTo(WorkerLink o) {
        if (memoryInUse != o.memoryInUse) {
            return memoryInUse - o.memoryInUse;
        } else {
            return this.hashCode() - o.hashCode();
        }
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
