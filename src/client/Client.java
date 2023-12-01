import messages.AuthReply;
import messages.AuthRequest;
import messages.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (var socket = new Socket("localhost", 1337)) {
            var in = new DataInputStream(socket.getInputStream());
            var out = new DataOutputStream(socket.getOutputStream());
            var scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Enter username: ");
                var username = scanner.nextLine();
                System.out.print("Enter password: ");
                var password = scanner.nextLine();

                new AuthRequest(username, password).serialize(out);

                while (Type.deserialize(in) != messages.Type.AUTH_REPLY) {
                    in.readAllBytes();
                }

                var authReply = AuthReply.deserialize(in);
                if (authReply.success()) {
                    System.out.println("Login successful.");
                    break;
                } else {
                    System.out.println("Failed to login.");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
