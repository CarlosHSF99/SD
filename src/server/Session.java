package server;

import concurrentUtils.BoundedBuffer;
import connection.messages.*;
import connection.multiplexer.Frame;
import connection.multiplexer.TaggedConnection;
import connection.utils.*;
import sd23.JobFunctionException;

import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable {
    private final Socket socket;
    private final Auth auth;
    private final Scheduler scheduler;
    private final BoundedBuffer<Runnable> taskBuffer;

    public Session(Socket socket, BoundedBuffer<Runnable> taskBuffer, Auth auth, Scheduler scheduler) {
        this.socket = socket;
        this.auth = auth;
        this.scheduler = scheduler;
        this.taskBuffer = taskBuffer;
    }

    @Override
    public void run() {
        try (var connection = new TaggedConnection(socket)) {
            authenticate(connection);

            while (true) {
                var frame = connection.receive();
                var message = frame.message();

                taskBuffer.put(() -> {
                    try {
                        switch (message.type()) {
                            case JOB_REQUEST -> connection.send(frame.tag(), runJob((JobRequest) message));
                            case STATUS_REQUEST -> connection.send(frame.tag(), new StatusReply(scheduler.getAvailableMemory(), scheduler.getPendingJobs()));
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

    private Message runJob(JobRequest jobRequest) throws InterruptedException {
        System.out.println("Running job");
        try {
            return new JobReplyOk(scheduler.addJob(jobRequest.code()));
        } catch (JobTooBigException e) {
            return new JobReplyError(0, "Not enough memory");
        } catch (JobFunctionException e) {
            return new JobReplyError(e.getCode(), e.getMessage());
        }
    }
}
