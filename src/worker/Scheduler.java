package worker;

import messages.JobRequest;
import sd23.JobFunction;
import sd23.JobFunctionException;
import server.JobTooBigException;

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

    public byte[] addJob(JobRequest job) throws InterruptedException, JobFunctionException, JobTooBigException {
        lock.lock();
        try {
            if (job.memory() > memoryCapacity) {
                throw new JobTooBigException();
            }

            var ticket = nextTicket++;
            while (memoryInUse + job.memory() > memoryCapacity || ticket > turn) {
                cond.await();
            }
            turn++;
            memoryInUse += job.memory();
            pendingJobs++;
        } finally {
            lock.unlock();
        }

        try {
            return JobFunction.execute(job.code());
        } finally {
            lock.lock();
            try {
                memoryInUse -= job.memory();
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
