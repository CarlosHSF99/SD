package server;

import connection.messages.JobReplyError;
import connection.messages.JobRequest;
import connection.multiplexer.TaggedConnection;
import connection.utils.Message;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MasterScheduler {
    private final WorkerPool workerPool = new WorkerPool();
    // maybe discriminate lock for availableMemory and pendingJobs
    private final Lock lock = new ReentrantLock();
    private int pendingJobs = 0;
    private int availableMemory = 0;

    public void addWorker(TaggedConnection connection, int memory) {
        lock.lock();
        try {
            workerPool.add(new WorkerLink(connection, memory));
            availableMemory += memory;
        } finally {
            lock.unlock();
        }

        System.out.println("Added worker with " + memory + " memory");
    }

    public Message runJob(JobRequest jobRequest) throws InterruptedException {
        try {
            lock.lock();
            try {
                pendingJobs++;
                availableMemory -= jobRequest.memory();
            } finally {
                lock.unlock();
            }

            Message reply;
            WorkerLink workerLink;
            while (true) {
                try {
                    workerLink = workerPool.getWorker(jobRequest.memory());
                } catch (NoSuitableWorkerException e) {
                    return new JobReplyError(0, "Not enough memory");
                }
                try {
                    System.out.println("Running job remotely");
                    reply = workerLink.runJob(jobRequest);
                    break;
                } catch (IOException e) {
                    System.out.println("Worker disconnected");
                    workerPool.remove(workerLink);
                }
            }
            workerPool.update(workerLink, jobRequest.memory());
            return reply;

        } finally {
            lock.lock();
            try {
                pendingJobs--;
                availableMemory += jobRequest.memory();
            } finally {
                lock.unlock();
            }
        }
    }

    public int availableMemory() {
        lock.lock();
        try {
            return availableMemory;
        } finally {
            lock.unlock();
        }
    }

    public int pendingJobs() {
        lock.lock();
        try {
            return pendingJobs;
        } finally {
            lock.unlock();
        }
    }
}
