package concurrentUtils;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GrowableThreadPoolService implements ThreadPoolService {
    private final ConcurrentBuffer<Runnable> taskBuffer = new ConcurrentBuffer<>();
    private final Lock lock = new ReentrantLock();
    private final Condition allThreadsRunning = lock.newCondition();
    private final int initialPoolSize;
    private int poolSize = 0;
    private int runningThreads = 0;

    public GrowableThreadPoolService(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }

    public GrowableThreadPoolService() {
        this(2);
    }

    @Override
    public void submit(Runnable task) {
        taskBuffer.put(task);
    }

    @Override
    public void start() {
        startThreads(initialPoolSize);

        var thread = new Thread(() -> {
            while (true) {
                lock.lock();
                try {
                    while (runningThreads < poolSize) {
                        allThreadsRunning.await();
                    }
                    startThreads(poolSize);
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                } finally {
                    lock.unlock();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void startThreads(int numberOfThreads) {
        for (int i = 0; i < numberOfThreads; i++) {
            var thread = new Thread(() -> {
                while (true) {
                    try {
                        var task = taskBuffer.get();

                        lock.lock();
                        try {
                            runningThreads++;
                            if (runningThreads == poolSize) {
                                allThreadsRunning.signal();
                            }
                        } finally {
                            lock.unlock();
                        }

                        task.run();

                        lock.lock();
                        try {
                            runningThreads--;
                        } finally {
                            lock.unlock();
                        }
                    } catch (InterruptedException ie) {
                        throw new RuntimeException(ie);
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
        poolSize += numberOfThreads;
    }
}
