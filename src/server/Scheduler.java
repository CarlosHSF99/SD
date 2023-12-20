package server;

import sd23.JobFunction;
import sd23.JobFunctionException;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Scheduler {
    private final int memoryCapacity;
    private final Lock lock = new ReentrantLock();
    private final Condition cond = lock.newCondition();
    private long turn = Long.MIN_VALUE;
    private long nextTicket = Long.MIN_VALUE;
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
        lock.lock();
        try {
            return pendingJobs;
        } finally {
            lock.unlock();
        }
    }

    public int getAvailableMemory() {
        lock.lock();
        try {
            return memoryCapacity - memoryInUse;
        } finally {
            lock.unlock();
        }
    }
}
