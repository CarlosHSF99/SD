package faas;

import connection.messages.*;
import connection.multiplexer.MultiplexedConnection;

import java.io.IOException;
import java.net.Socket;

public class FaaS implements AutoCloseable {
    private final MultiplexedConnection connection;

    public FaaS(String username, String password) throws AuthFailedException, IOException, InterruptedException {
        this.connection = new MultiplexedConnection(new Socket("localhost", 1337));
        new Thread(connection).start();
        var tag = connection.send(new ClientHandshake(username, password));
        if (!((AuthReply) connection.receive(tag)).success()) {
            throw new AuthFailedException();
        }
    }

    public byte[] executeJob(byte[] job, int memory) throws IOException, InterruptedException, JobFailedException {
        var tag = connection.send(new JobRequest(job, memory));
        var message = connection.receive(tag);
        switch (message.type()) {
            case JOB_REPLY_OK -> {
                return ((JobReplyOk) message).output();
            }
            case JOB_REPLY_ERROR -> {
                var jobReplyError = (JobReplyError) message;
                throw new JobFailedException(jobReplyError.code(), jobReplyError.message());
            }
        }
        return ((JobReplyOk) connection.receive(tag)).output();
    }

    public Status getStatus() throws IOException, InterruptedException {
        var tag = connection.send(new StatusRequest());
        StatusReply message = (StatusReply) connection.receive(tag);
        return new Status(message.availableMemory(), message.pendingJobs());
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
