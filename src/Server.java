import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;

import messages.AuthReply;
import messages.AuthRequest;
import messages.Type;

public class Server {
    public static void main(String[] args) throws IOException {
        HashMap<String, String> auth = new HashMap<>();
        try (var serverSocket = new ServerSocket(1337)) {
            while (true) {
                var socket = serverSocket.accept();
                var in = new DataInputStream(socket.getInputStream());
                var out = new DataOutputStream(socket.getOutputStream());

                while (true) {
                    while (Type.deserialize(in) != Type.AUTH_REQUEST) {
                        in.readAllBytes();
                    }

                    var login = AuthRequest.deserialize(in);
                    var username = login.username();
                    var password = login.password();

                    if (auth.containsKey(username)) {
                        if (auth.get(username).equals(password)) {
                            System.out.println("User " + username + " logged in.");
                            new AuthReply(true).serialize(out);
                            break;
                        } else {
                            System.out.println("User " + username + " failed to log in.");
                            new AuthReply(false).serialize(out);
                        }
                    } else {
                        auth.put(username, password);
                        System.out.println("User " + username + " registered.");
                        new AuthReply(true).serialize(out);
                        break;
                    }
                }
            }
        }
    }
}
