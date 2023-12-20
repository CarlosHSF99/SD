package client;

import connection.messages.*;
import connection.multiplexer.MultiplexedConnection;
import connection.utils.Message;
import connection.utils.Type;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try (var connection = new MultiplexedConnection(new Socket("localhost", 1337))) {
            new Thread(connection).start();
            authenticate(connection);

            var scanner = new Scanner(System.in);
            while (true) {
                var line = scanner.nextLine();
                Thread.startVirtualThread(() -> {
                    try {
                        var tag = send(connection, line);
                        handleMessage(connection.receive(tag));
                    } catch (InterruptedException | IOException e) {
                        System.out.println("Error receiving message");
                    } catch (IllegalArgumentException ignored) {
                    }
                });
            }
        } catch (IOException | NoSuchElementException | InterruptedException e) {
            var exceptionMessage = e.getMessage();
            System.out.println("Connection ended" + (exceptionMessage != null ? " with error: " + exceptionMessage : "."));
        }
    }

    private static void authenticate(MultiplexedConnection connection) throws IOException, NoSuchElementException, InterruptedException {
        var scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter username: ");
            var username = scanner.nextLine();
            System.out.print("Enter password: ");
            var password = scanner.nextLine();

            var tag = connection.send(new AuthRequest(username, password));

            Message message;
            while ((message = connection.receive(tag)).type() != Type.AUTH_REPLY) ;
            var authReply = (AuthReply) message;

            if (authReply.success()) {
                System.out.println("Login successful.");
                break;
            } else {
                System.out.println("Failed to login.");
            }
        }
    }

    private static int send(MultiplexedConnection connection, String line) throws IOException, IllegalArgumentException {
        var tokens = line.split(" ");

        switch (tokens[0]) {
            case "exec" -> {
                return connection.send(new JobRequest(Files.readAllBytes(Path.of(tokens[1])), Integer.parseInt(tokens[2])));
            }
            default -> {
                System.out.println("Unknown command");
                throw new IllegalArgumentException(tokens[0]);
            }
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
