package server;

import messages.JobReplyError;
import messages.JobRequest;
import connectionUtils.TaggedConnection;
import messages.utils.Message;

import java.io.IOException;

public class MasterScheduler {
    private final WorkerPool workerPool = new WorkerPool();

    public void addWorker(TaggedConnection connection, int memory) {
        workerPool.add(new WorkerLink(connection, memory, workerPool));
    }

    public Message runJob(JobRequest jobRequest) throws InterruptedException {
        Message reply;
        WorkerLink workerLink;
        while (true) {
            try {
                workerLink = workerPool.selectWorker(jobRequest.memory());
            } catch (NoSuitableWorkerException e) {
                return new JobReplyError(0, "Not enough memory");
            }
            try {
                reply = workerLink.runJob(jobRequest);
                break;
            } catch (IOException ignored) {
            }
        }
        workerPool.updateWorker(workerLink, jobRequest.memory());
        return reply;
    }

    public int availableMemory() {
        return Math.max(workerPool.availableMemory(), 0);
    }

    public int pendingJobs() {
        return workerPool.pendingJobs();
    }

    public int maxJobMemory() {
        return workerPool.maxJobMemory();
    }
}
