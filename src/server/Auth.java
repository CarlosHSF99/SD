package server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Auth {
    private final Map<String, String> registry = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    public boolean authenticate(String username, String password) {
        String storedPassword;
        readLock.lock();
        try {
            storedPassword = registry.get(username);
        } finally {
            readLock.unlock();
        }
        if (storedPassword != null) {
            return storedPassword.equals(password);
        }
        writeLock.lock();
        try {
            registry.put(username, password);
        } finally {
            writeLock.unlock();
        }
        return true;
    }
}
