package server;

import connection.messages.*;
import connection.utils.Connection;
import connection.utils.Message;
import connection.utils.Type;
import sd23.JobFunctionException;

import java.io.IOException;
import java.net.Socket;

public class Session implements Runnable {
    private final Socket socket;
    private final Auth auth;
    private final Scheduler scheduler;

    public Session(Socket socket, Auth auth, Scheduler scheduler) {
        this.socket = socket;
        this.auth = auth;
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        try (var connection = new Connection(socket)) {
            authenticate(connection);

            while (true) {
                var message = connection.receive();

                switch (message.type()) {
                    case JOB_REQUEST -> connection.send(runJob((JobRequest) message));
                    default -> System.out.println("Received unknown message type");
                }
            }
        } catch (IOException e) {
            System.out.println("Connection ended.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void authenticate(Connection connection) throws IOException {
        while (true) {
            Message message;
            while ((message = connection.receive()).type() != Type.AUTH_REQUEST);
            var authRequest = (AuthRequest) message;

            if (auth.authenticate(authRequest.username(), authRequest.password())) {
                break;
            } else {
                connection.send(new AuthReply(false));
            }
        }
        connection.send(new AuthReply(true));
    }

    private Message runJob(JobRequest jobRequest) throws IOException, InterruptedException {
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
