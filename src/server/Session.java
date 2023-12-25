package server;

import concurrentUtils.BoundedBuffer;
import connection.messages.AuthReply;
import connection.messages.AuthRequest;
import connection.messages.JobRequest;
import connection.messages.WorkerHandshake;
import connection.multiplexer.Frame;
import connection.multiplexer.TaggedConnection;
import connection.utils.Type;

import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable {
    private final Socket socket;
    private final Auth auth;
    private final MasterScheduler masterScheduler;
    private final BoundedBuffer<Runnable> taskBuffer;

    public Session(Socket socket, BoundedBuffer<Runnable> taskBuffer, Auth auth, MasterScheduler masterScheduler) {
        this.socket = socket;
        this.auth = auth;
        this.masterScheduler = masterScheduler;
        this.taskBuffer = taskBuffer;
    }

    @Override
    public void run() {
        try {
            var connection = new TaggedConnection(socket);
            var handshakeFrame = connection.receive();
            var handshake = handshakeFrame.message();

            switch (handshake.type()) {
                case CLIENT_HANDSHAKE -> clientService(connection);
                case WORKER_HANDSHAKE -> masterScheduler.addWorker(connection, ((WorkerHandshake) handshake).memory());
                default -> System.out.println("Received unknown handshake type");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void clientService(TaggedConnection taggedConnection) {
        try (var connection = taggedConnection) {
            authenticate(connection);

            while (true) {
                var frame = connection.receive();
                var message = frame.message();

                taskBuffer.put(() -> {
                    try {
                        switch (message.type()) {
                            case JOB_REQUEST -> connection.send(frame.tag(), masterScheduler.runJob((JobRequest) message));
                            default -> System.out.println("Received unknown message type");
                        }
                    } catch (IOException | InterruptedException e) {
                        var exceptionMessage = e.getMessage();
                        System.out.println("Error processing request" + (exceptionMessage != null ? ": " + exceptionMessage : "."));
                    }
                });
            }
        } catch (IOException | InterruptedException e) {
            var exceptionMessage = e.getMessage();
            System.out.println("Connection ended" + (exceptionMessage != null ? " with error: " + exceptionMessage : "."));
        }
    }

    private void authenticate(TaggedConnection connection) throws IOException {
        Frame frame;
        while (true) {
            while ((frame = connection.receive()).message().type() != Type.AUTH_REQUEST) ;
            var authRequest = (AuthRequest) frame.message();

            if (auth.authenticate(authRequest.username(), authRequest.password())) {
                break;
            } else {
                connection.send(frame.tag(), new AuthReply(false));
            }
        }
        connection.send(frame.tag(), new AuthReply(true));
    }
}
