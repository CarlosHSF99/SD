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
    private IOException ioException = null;

    public void put(Frame frame) {
        lock.lock();
        try {
            if (ioException == null) {
                map.computeIfAbsent(frame.tag(), t -> new MessageBuffer(lock.newCondition())).put(frame.message());
            }
        } finally {
            lock.unlock();
        }
    }

    public Message get(int tag) throws IOException, InterruptedException {
        lock.lock();
        try {
            if (ioException != null) {
                throw ioException;
            }
            return map.computeIfAbsent(tag, t -> new MessageBuffer(lock.newCondition())).get();
        } finally {
            map.remove(tag);
            lock.unlock();
        }
    }

    public void kill(IOException ioe) {
        lock.lock();
        try {
            ioException = ioe;
            map.values().forEach(messageBuffer -> messageBuffer.kill(ioe));
        } finally {
            lock.unlock();
        }
    }
}
