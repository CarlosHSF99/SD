package server;

import connection.messages.JobRequest;
import connection.multiplexer.TaggedConnection;
import connection.multiplexer.WorkerMultiplexedConnection;
import connection.utils.Message;

import java.io.IOException;

public class WorkerLink implements Runnable, AutoCloseable, Comparable<WorkerLink> {
    private final WorkerMultiplexedConnection connection;
    private final int totalMemory;
    private int memoryInUse = 0;
    private int pendingJobs = 0;

    public WorkerLink(TaggedConnection connection, int totalMemory, WorkerPool workerPool) {
        this.connection = new WorkerMultiplexedConnection(connection, workerPool, this);
        this.totalMemory = totalMemory;
    }

    @Override
    public void run() {
        new Thread(connection).start();
    }

    public Message runJob(JobRequest jobRequest) throws IOException, InterruptedException {
        try {
            pendingJobs++;
            return connection.receive(connection.send(jobRequest));
        } finally {
            pendingJobs--;
        }
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

    public int pendingJobs() {
        return pendingJobs;
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
