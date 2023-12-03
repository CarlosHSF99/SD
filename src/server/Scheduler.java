package server;

import sd23.JobFunction;
import sd23.JobFunctionException;

import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Scheduler {
    private final int memoryCapacity;
    private final Lock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    private long turn = 0;
    private long nextTicket = 0;
    private int memoryInUse = 0;
    private int pendingJobs = 0;

    public Scheduler(int memoryCapacity) {
        this.memoryCapacity = memoryCapacity;
    }

    public byte[] addJob(byte[] job) throws InterruptedException, JobFunctionException, JobTooBigException {
        if (job.length > memoryCapacity) {
            throw new JobTooBigException();
        }

        lock.lock();
        try {
            var ticket = nextTicket++;
            while (memoryInUse + job.length > memoryCapacity || ticket > turn) {
                System.out.println("suspend: " + Thread.currentThread().threadId());
                cond.await();
            }
            turn++;
            memoryInUse += job.length;
            pendingJobs++;
        } finally {
            lock.unlock();
        }

        try {
            return JobFunction.execute(job);
        } finally {
            lock.lock();
            try {
                memoryInUse -= job.length;
                pendingJobs--;
                cond.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }

    public int getPendingJobs() {
        try {
            lock.lock();
            return pendingJobs;
        } finally {
            lock.unlock();
        }
    }

    public int getMemoryInUse() {
        try {
            return memoryInUse;
        } finally {
            lock.unlock();
        }
    }
}
