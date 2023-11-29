package concurrentUtils;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class BoundedQueue<E> extends LinkedList<E> {
    private final int bound;

    public BoundedQueue(int bound) {
        this.bound = bound;
    }

    public boolean isFull() {
        return size() == bound;
    }
}

public class BoundedBuffer<E> {
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final BoundedQueue<E> buffer;

    public BoundedBuffer(int bound) {
        this.buffer = new BoundedQueue<>(bound);
    }

    public void put(E value) throws InterruptedException {
        lock.lock();
        try {
            while (buffer.isFull()) notFull.await();
            buffer.add(value);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public E get() throws InterruptedException {
        lock.lock();
        try {
            while (buffer.isEmpty()) notEmpty.await();
            E value = buffer.remove();
            notFull.signal();
            return value;
        } finally {
            lock.unlock();
        }
    }
}
