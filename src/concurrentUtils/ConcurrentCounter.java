package concurrentUtils;

import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentCounter {
    private final ReentrantLock lock = new ReentrantLock();
    private int counter = 0;

    public int get() {
        lock.lock();
        try {
            return counter;
        } finally {
            lock.unlock();
        }
    }

    public int next() {
        lock.lock();
        try {
            return counter++;
        } finally {
            lock.unlock();
        }
    }
}
