package client;

import connection.messages.*;
import connection.utils.Connection;
import connection.utils.Message;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try (var socket = new Socket("localhost", 1337);
             var in = new DataInputStream(socket.getInputStream());
             var out = new DataOutputStream(socket.getOutputStream())) {
            authenticate(in, out);

            while (true) {
                send(out);
                Thread.startVirtualThread(() -> {
                    try {
                        receive(in);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException | NoSuchElementException e) {
            System.out.println("Connection ended.");
        }
    }

    private static void authenticate(DataInputStream in, DataOutputStream out) throws IOException, NoSuchElementException {
        var scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter username: ");
            var username = scanner.nextLine();
            System.out.print("Enter password: ");
            var password = scanner.nextLine();

            new Connection(new AuthRequest(username, password)).send(out);

            Message message;
            while ((message = Connection.receive(in)).getType() != Type.AUTH_REPLY);
            var authReply = (AuthReply) message;

            if (authReply.success()) {
                System.out.println("Login successful.");
                break;
            } else {
                System.out.println("Failed to login.");
            }
        }
    }

    private static void send(DataOutputStream out) throws IOException {
        var scanner = new Scanner(System.in);
        var line = scanner.nextLine();
        var tokens = line.split(" ");

        switch (tokens[0]) {
            case "exec" -> {
                new Connection(new JobRequest(Files.readAllBytes(Path.of(tokens[1])))).send(out);
            }
            default -> System.out.println("Unknown command");
        }
    }

    private static void receive(DataInputStream in) throws IOException {
        var message = Connection.receive(in);
        switch (message.getType()) {
            case JOB_REPLY_OK -> {
                System.out.println("Job finished successfully.");
                try (var fos = new FileOutputStream("out.txt")) {
                    fos.write(((JobReplyOk) message).output());
                }
            }
            case JOB_REPLY_ERROR -> {
                var jobReplyError = (JobReplyError) message;
                System.out.println("Job failed.\n\tCode: " + jobReplyError.code() + "\n\tMessage: " + jobReplyError.message());
            }
            default -> System.out.println("Received unknown message type");
        }
    }
}
