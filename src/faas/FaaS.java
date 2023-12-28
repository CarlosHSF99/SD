package faas;

import connection.messages.*;
import connection.multiplexer.MultiplexedConnection;
import connection.multiplexer.TaggedConnection;

import java.io.IOException;
import java.net.Socket;

public class FaaS implements AutoCloseable {
    private final MultiplexedConnection connection;

    public FaaS(String username, String password) throws IOException, AuthFailedException {
        var taggedConnection = new TaggedConnection(new Socket("localhost", 1337));
        taggedConnection.send(0, new ClientHandshake(username, password));
        if (!((AuthReply) taggedConnection.receive().message()).success()) {
            throw new AuthFailedException();
        }
        this.connection = new MultiplexedConnection(taggedConnection);
    }

    public void start() throws AuthFailedException, IOException, InterruptedException {
        new Thread(connection).start();
    }

    public byte[] executeJob(byte[] job, int memory) throws IOException, InterruptedException, JobFailedException {
        var tag = connection.send(new JobRequest(job, memory));
        var reply = connection.receive(tag);
        switch (reply) {
            case JobReplyOk jobReplyOk -> { return jobReplyOk.output(); }
            case JobReplyError jobReplyError -> throw new JobFailedException(jobReplyError.code(), jobReplyError.message());
            default -> throw new RuntimeException("Unexpected message type");
        }
    }

    public Status getStatus() throws IOException, InterruptedException {
        var tag = connection.send(new StatusRequest());
        StatusReply status = (StatusReply) connection.receive(tag);
        return new Status(status.availableMemory(), status.pendingJobs());
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
