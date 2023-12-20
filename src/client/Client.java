package client;

import concurrentUtils.BoundedBuffer;
import connection.messages.*;
import connection.utils.Connection;
import connection.utils.Message;
import connection.utils.Type;
import server.ClientReader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        var replyBuffer = new BoundedBuffer<Message>(128);

        try (var connection = new Connection(new Socket("localhost", 1337))) {
            authenticate(connection);

            var clientReader = new Thread(new ClientReader(connection, replyBuffer));
            clientReader.start();

            var scanner = new Scanner(System.in);
            while (true) {
                send(connection, scanner.nextLine());
                Thread.startVirtualThread(() -> {
                    try {
                        handleMessage(replyBuffer.get());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException | NoSuchElementException e) {
            var exceptionMessage = e.getMessage();
            System.out.println("Connection ended" + (exceptionMessage != null ? " with error: " + exceptionMessage : "."));
        }
    }

    private static void authenticate(Connection connection) throws IOException, NoSuchElementException {
        var scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter username: ");
            var username = scanner.nextLine();
            System.out.print("Enter password: ");
            var password = scanner.nextLine();

            connection.send(new AuthRequest(username, password));

            Message message;
            while ((message = connection.receive()).type() != Type.AUTH_REPLY) ;
            var authReply = (AuthReply) message;

            if (authReply.success()) {
                System.out.println("Login successful.");
                break;
            } else {
                System.out.println("Failed to login.");
            }
        }
    }

    private static void send(Connection connection, String line) throws IOException {
        var tokens = line.split(" ");

        switch (tokens[0]) {
            case "exec" -> {
                connection.send(new JobRequest(Files.readAllBytes(Path.of(tokens[1]))));
            }
            default -> System.out.println("Unknown command");
        }
    }

    private static void handleMessage(Message message) {
        switch (message.type()) {
            case JOB_REPLY_OK -> {
                System.out.println("Job finished successfully.");
                try (var fos = new FileOutputStream("out.txt")) {
                    fos.write(((JobReplyOk) message).output());
                } catch (IOException e) {
                    throw new RuntimeException(e);
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
