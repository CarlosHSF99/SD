package server;

import messages.AuthReply;
import messages.AuthRequest;
import messages.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
