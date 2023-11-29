package concurrentUtils;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool {
    private final List<Thread> threads;

    public ThreadPool(int poolSize, BoundedBuffer<Runnable> buffer) {
        this.threads = new ArrayList<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            var thread = new Thread(() -> {
                while (true) {
                    try {
                        buffer.get().run();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.setDaemon(true);
            threads.add(thread);
        }
    }

    public void start() {
        threads.forEach(Thread::start);
    }

    public void join() throws InterruptedException {
        for (var thread : threads) {
            thread.join();
        }
    }
}
