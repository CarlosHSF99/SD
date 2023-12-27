import faas.AuthFailedException;
import faas.FaaS;
import faas.JobFailedException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        var scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            System.out.print("Enter password: ");
            String password = scanner.nextLine();
            try (var faas = new FaaS(username, password)) {
                while (true) {
                    var tokens = scanner.nextLine().split(" ");
                    new Thread(() -> {
                        try {
                            switch (tokens[0]) {
                                case "exec" -> {
                                    try {
                                        var filePath = tokens[1];
                                        var job = Files.readAllBytes(Path.of(filePath));
                                        var memory = Integer.parseInt(tokens[2]);
                                        var output = faas.executeJob(job, memory);
                                        System.out.println("Job finished.");
                                        try (var fos = new FileOutputStream(filePath + ".out")) {
                                            System.out.println("Writing output to file");
                                            fos.write(output);
                                        } catch (IOException e) {
                                            System.out.println("Error writing output to file");
                                        }
                                    } catch (JobFailedException jfe) {
                                        System.out.println("Job failed.\n\tCode: " + jfe.code() + "\n\tMessage: " + jfe.message());
                                    }
                                }
                                case "status" -> {
                                    var status = faas.getStatus();
                                    System.out.println("Status:\n\tAvailable memory: " + status.availableMemory() + "\n\tNumber of pending tasks: " + status.pendingJobs());
                                }
                                default -> System.out.println("Unknown command");
                            }
                        } catch (IOException | InterruptedException e) {
                            System.out.println("Error receiving message");
                        }
                    }).start();
                }
            } catch (IOException | InterruptedException e) {
                var exceptionMessage = e.getMessage();
                System.out.println("Connection ended" + (exceptionMessage != null ? " with error: " + exceptionMessage : "."));
            } catch (AuthFailedException e) {
                System.out.println("Authentication failed.");
                continue;
            }
            break;
        }
    }
}
