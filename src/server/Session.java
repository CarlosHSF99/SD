package server;

import messages.*;
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
                switch (Type.deserialize(in)) {
                    case JOB_REQUEST -> job(in, out);
                    default -> System.out.println("Received unknown message type");
                }
            }
        } catch (IOException e) {
            // throw new RuntimeException(e);
            System.out.println("Connection ended.");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void authenticate(DataInputStream in, DataOutputStream out) throws IOException {
        while (true) {
            while (Type.deserialize(in) != Type.AUTH_REQUEST) {
                in.readAllBytes(); // this seems problematic
            }

            var login = AuthRequest.deserialize(in);

            if (auth.authenticate(login.username(), login.password())) {
                break;
            } else {
                new AuthReply(false).serialize(out);
            }
        }
        new AuthReply(true).serialize(out);
    }

    private void job(DataInputStream in, DataOutputStream out) throws IOException, InterruptedException {
        try {
            new JobReplyOk(scheduler.addJob(JobRequest.deserialize(in).code())).serialize(out);
        } catch (JobTooBigException e) {
            new JobReplyError(0, "Not enough memory").serialize(out);
        } catch (JobFunctionException e) {
            new JobReplyError(e.getCode(), e.getMessage()).serialize(out);
        }
    }
}
