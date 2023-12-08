package server;

import connection.messages.*;
import connection.utils.Message;
import connection.utils.Payload;
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
                var message = Message.receive(in);

                switch (message.type()) {
                    case JOB_REQUEST -> new Message(runJob((JobRequest) message.payload())).send(out);
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
            while ((message = Message.receive(in)).type() != Type.AUTH_REQUEST);
            var authRequest = (AuthRequest) message.payload();

            if (auth.authenticate(authRequest.username(), authRequest.password())) {
                break;
            } else {
                new Message(new AuthReply(false)).send(out);
            }
        }
        new Message(new AuthReply(true)).send(out);
    }

    private Payload runJob(JobRequest jobRequest) throws IOException, InterruptedException {
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
