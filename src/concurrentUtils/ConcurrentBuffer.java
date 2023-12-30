package concurrentUtils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentBuffer<E> {
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Queue<E> buffer = new LinkedList<>();

    public void put(E value) {
        lock.lock();
        try {
            buffer.add(value);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public E get() throws InterruptedException {
        lock.lock();
        try {
            while (buffer.isEmpty()) {
                notEmpty.await();
            }
            return buffer.remove();
        } finally {
            lock.unlock();
        }
    }
}
