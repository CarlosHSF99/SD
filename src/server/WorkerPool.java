package server;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class WorkerPool {
    private final Set<WorkerLink> workerLinks = new TreeSet<>();
    private final Lock lock = new ReentrantLock();

    public void add(WorkerLink workerLink) {
        lock.lock();
        try {
            workerLinks.add(workerLink);
            new Thread(workerLink).start();
        } finally {
            lock.unlock();
        }
    }

    public void remove(WorkerLink workerLink) {
        lock.lock();
        try {
            workerLink.close();
            workerLinks.remove(workerLink);
        } catch (IOException ignored) {
        } finally {
            lock.unlock();
        }
    }

    public WorkerLink selectWorker(int memory) throws NoSuitableWorkerException {
        lock.lock();
        try {
            return workerLinks.stream()
                    .filter(workerLink -> workerLink.totalMemory() >= memory)
                    .findFirst()
                    .map(workerLink -> {
                        workerLinks.remove(workerLink);
                        workerLink.allocMemory(memory);
                        workerLinks.add(workerLink);
                        return workerLink;
                    })
                    .orElseThrow(NoSuitableWorkerException::new);
        } finally {
            lock.unlock();
        }
    }

    public void updateWorker(WorkerLink workerLink, int memory) {
        lock.lock();
        try {
            workerLinks.remove(workerLink);
            workerLink.freeMemory(memory);
            workerLinks.add(workerLink);
        } finally {
            lock.unlock();
        }
    }

    public int availableMemory() {
        lock.lock();
        try {
            return workerLinks.stream().mapToInt(WorkerLink::availableMemory).sum();
        } finally {
            lock.unlock();
        }
    }

    public int pendingJobs() {
        lock.lock();
        try {
            return workerLinks.stream().mapToInt(WorkerLink::pendingJobs).sum();
        } finally {
            lock.unlock();
        }
    }

    public int maxJobMemory() {
        lock.lock();
        try {
            return workerLinks.stream().map(WorkerLink::totalMemory).max(Integer::compareTo).orElse(0);
        } finally {
            lock.unlock();
        }
    }
}
