package server;

import concurrentUtils.BoundedBuffer;
import connection.messages.*;
import connection.multiplexer.Frame;
import connection.multiplexer.TaggedConnection;

import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable {
    private final Socket socket;
    private final Auth auth;
    private final MasterScheduler scheduler;
    private final BoundedBuffer<Runnable> taskBuffer;

    public Session(Socket socket, BoundedBuffer<Runnable> taskBuffer, Auth auth, MasterScheduler scheduler) {
        this.socket = socket;
        this.auth = auth;
        this.scheduler = scheduler;
        this.taskBuffer = taskBuffer;
    }

    @Override
    public void run() {
        try {
            var connection = new TaggedConnection(socket);
            var handshakeFrame = connection.receive();
            var handshake = handshakeFrame.message();

            switch (handshake.type()) {
                case CLIENT_HANDSHAKE -> clientService(connection, handshakeFrame);
                case WORKER_HANDSHAKE -> scheduler.addWorker(connection, ((WorkerHandshake) handshake).memory());
                default -> System.out.println("Received unknown handshake type");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void clientService(TaggedConnection taggedConnection, Frame handshakeFrame) {
        var handshake = (ClientHandshake) handshakeFrame.message();
        var username = handshake.username();
        var password = handshake.password();

        System.out.println("User " + username + " connected");

        try (var connection = taggedConnection) {

            if (auth.authenticate(username, password)) {
                System.out.println("User " + username + " authenticated");
                connection.send(handshakeFrame.tag(), new AuthReply(true));
            } else {
                System.out.println("User " + username + " authentication failed");
                connection.send(handshakeFrame.tag(), new AuthReply(false));
                return;
            }

            while (true) {
                var frame = connection.receive();
                var message = frame.message();

                taskBuffer.put(() -> {
                    try {
                        switch (message.type()) {
                            case JOB_REQUEST -> connection.send(frame.tag(), scheduler.runJob((JobRequest) message));
                            case STATUS_REQUEST -> connection.send(frame.tag(), new StatusReply(scheduler.availableMemory(), scheduler.pendingJobs()));
                            default -> System.out.println("Received unknown message type");
                        }
                    } catch (IOException | InterruptedException e) {
                        var exceptionMessage = e.getMessage();
                        System.out.println("Error processing request" + (exceptionMessage != null ? ": " + exceptionMessage : "."));
                    }
                });
            }
        } catch (IOException | InterruptedException e) {
            if (e.getMessage() != null) {
                System.out.println("User " + username + "'s connection ended with error: " + e.getMessage());
            } else {
                System.out.println("User " + username + " disconnected");
            }
        }
    }
}
