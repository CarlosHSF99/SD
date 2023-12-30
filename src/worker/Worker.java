package worker;

import concurrentUtils.GrowableThreadPoolService;
import messages.JobReplyError;
import messages.JobReplyOk;
import messages.JobRequest;
import messages.WorkerHandshake;
import connectionUtils.TaggedConnection;
import messages.utils.Message;
import sd23.JobFunctionException;
import server.JobTooBigException;

import java.io.IOException;
import java.net.Socket;

public class Worker {
    public static void main(String[] args) {
        var memory = Integer.parseInt(args[0]);

        var scheduler = new Scheduler(memory);
        var threadPool = new GrowableThreadPoolService(4);
        threadPool.start();

        try (var connection = new TaggedConnection(new Socket("localhost", 1337))) {
            connection.send(0, new WorkerHandshake(memory));
            while (true) {
                var frame = connection.receive();
                System.out.println("Received job request");
                threadPool.submit(() -> {
                    if (frame.message() instanceof JobRequest jobRequest) {
                        try {
                            connection.send(frame.tag(), runJob(scheduler, jobRequest));
                        } catch (IOException | InterruptedException ignored) {
                        }
                    }
                });
            }
        } catch (IOException e) {
            var exceptionMessage = e.getMessage();
            System.out.println("Connection ended" + (exceptionMessage != null ? " with error: " + exceptionMessage : "."));
        }
    }

    private static Message runJob(Scheduler scheduler, JobRequest jobRequest) throws InterruptedException {
        try {
            return new JobReplyOk(scheduler.addJob(jobRequest));
        } catch (JobTooBigException e) {
            return new JobReplyError(0, "Not enough memory");
        } catch (JobFunctionException e) {
            return new JobReplyError(e.getCode(), e.getMessage());
        }
    }
}
