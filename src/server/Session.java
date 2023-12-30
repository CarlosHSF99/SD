package server;

import concurrentUtils.ThreadPoolService;
import messages.*;
import connectionUtils.Frame;
import connectionUtils.TaggedConnection;

import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable {
    private final Socket socket;
    private final Auth auth;
    private final MasterScheduler scheduler;
    private final ThreadPoolService threadPool;

    public Session(Socket socket, ThreadPoolService threadPool, Auth auth, MasterScheduler scheduler) {
        this.socket = socket;
        this.auth = auth;
        this.scheduler = scheduler;
        this.threadPool = threadPool;
    }

    @Override
    public void run() {
        try {
            var connection = new TaggedConnection(socket);
            var handshakeFrame = connection.receive();

            switch (handshakeFrame.message()) {
                case UserHandshake __ -> userService(connection, handshakeFrame);
                case WorkerHandshake workerHandshake -> workerService(connection, workerHandshake);
                default -> System.out.println("Received unknown handshake type");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void userService(TaggedConnection taggedConnection, Frame handshakeFrame) {
        var handshake = (UserHandshake) handshakeFrame.message();
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

                threadPool.submit(() -> {
                    try {
                        switch (message) {
                            case JobRequest jobRequest -> connection.send(frame.tag(), scheduler.runJob(jobRequest));
                            case StatusRequest __ -> connection.send(frame.tag(), new StatusReply(scheduler.availableMemory(), scheduler.maxJobMemory(), scheduler.pendingJobs()));
                            default -> System.out.println("Received unknown message type");
                        }
                    } catch (IOException | InterruptedException e) {
                        var exceptionMessage = e.getMessage();
                        System.out.println("Error processing request" + (exceptionMessage != null ? ": " + exceptionMessage : "."));
                    }
                });
            }
        } catch (IOException e) {
            if (e.getMessage() != null) {
                System.out.println("User " + username + "'s connection ended with error: " + e.getMessage());
            } else {
                System.out.println("User " + username + " disconnected");
            }
        }
    }

    private void workerService(TaggedConnection connection, WorkerHandshake workerHandshake) {
        System.out.println("Worker connected");
        scheduler.addWorker(connection, workerHandshake.memory());
    }
}
