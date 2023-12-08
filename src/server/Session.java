package server;

import connection.messages.*;
import connection.utils.Connection;
import connection.utils.Message;
import connection.utils.Type;
import sd23.JobFunctionException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
        try (var in = new DataInputStream(socket.getInputStream());
             var out = new DataOutputStream(socket.getOutputStream())) {
            authenticate(in, out);

            while (true) {
                var message = Connection.receive(in);

                switch (message.getType()) {
                    case JOB_REQUEST -> new Connection(runJob((JobRequest) message)).send(out);
                    default -> System.out.println("Received unknown message type");
                }
            }
        } catch (IOException e) {
            System.out.println("Connection ended.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void authenticate(DataInputStream in, DataOutputStream out) throws IOException {
        while (true) {
            Message message;
            while ((message = Connection.receive(in)).getType() != Type.AUTH_REQUEST);
            var authRequest = (AuthRequest) message;

            if (auth.authenticate(authRequest.username(), authRequest.password())) {
                break;
            } else {
                new Connection(new AuthReply(false)).send(out);
            }
        }
        new Connection(new AuthReply(true)).send(out);
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
