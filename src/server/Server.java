package server;

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
        var auth = new Auth();
        try (var serverSocket = new ServerSocket(1337)) {
            while (true) {
                var socket = serverSocket.accept();
                var session = new Thread(new Session(socket, auth));
                session.start();
            }
        }
    }
}
