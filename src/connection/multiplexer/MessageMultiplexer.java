package connection.multiplexer;

import connection.utils.Message;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageMultiplexer {
    private final Map<Integer, MessageBuffer> map = new HashMap<>();
    private final Lock lock = new ReentrantLock();
    private int firstValidTag = 0;

    public void put(Frame frame) {
        lock.lock();
        try {
            if (frame.tag() >= firstValidTag) {
                map.computeIfAbsent(frame.tag(), t -> new MessageBuffer(lock.newCondition())).put(frame.message());
            }
        } finally {
            lock.unlock();
        }
    }

    public Message get(int tag) throws IOException, InterruptedException {
        lock.lock();
        try {
            if (tag < firstValidTag) {
                throw new IOException();
            }
            return map.computeIfAbsent(tag, t -> new MessageBuffer(lock.newCondition())).get();
        } finally {
            map.remove(tag);
            lock.unlock();
        }
    }

    public void killUntil(int tag) {
        lock.lock();
        try {
            firstValidTag = tag;
            map.values().forEach(MessageBuffer::kill);
        } finally {
            lock.unlock();
        }
    }
}
