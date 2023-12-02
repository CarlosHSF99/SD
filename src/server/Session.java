package server;

import messages.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import sd23.JobFunction;
import sd23.JobFunctionException;

public class Session implements Runnable {
    private final Socket socket;
    private final Auth auth;

    public Session(Socket socket, Auth auth) {
        this.socket = socket;
        this.auth = auth;
    }

    @Override
    public void run() {
        try (var in = new DataInputStream(socket.getInputStream());
             var out = new DataOutputStream(socket.getOutputStream())) {
            authenticate(in, out);
            while (true) {
                switch (Type.deserialize(in)) {
                    case JOB_REQUEST -> {
                        var jobRequest = JobRequest.deserialize(in);
                        System.out.println("Received job:\n" + new String(jobRequest.code()));
                        try {
                            new JobReplyOk(JobFunction.execute(jobRequest.code())).serialize(out);
                        } catch (JobFunctionException e) {
                            new JobReplyError(e.getCode(), e.getMessage()).serialize(out);
                        }
                    }
                    default -> System.out.println("Received unknown message type");
                }
            }
        } catch (IOException e) {
            // throw new RuntimeException(e);
            System.out.println("Failed to authenticate.");
        }
    }

    private void authenticate(DataInputStream in, DataOutputStream out) throws IOException {
        while (true) {
            while (Type.deserialize(in) != Type.AUTH_REQUEST) {
                in.readAllBytes(); // this seems problematic
            }

            var login = AuthRequest.deserialize(in);
            var username = login.username();
            var password = login.password();

            if (auth.authenticate(username, password)) {
                break;
            } else {
                new AuthReply(false).serialize(out);
            }
        }
        new AuthReply(true).serialize(out);
    }
}
