package concurrentUtils;

public class FixedThreadPoolService implements ThreadPoolService {
    private final BoundedBuffer<Runnable> taskBuffer;
    private final int poolSize;

    public FixedThreadPoolService(int poolSize, int bufferSize) {
        this.taskBuffer = new BoundedBuffer<>(bufferSize);
        this.poolSize = poolSize;
    }

    @Override
    public void submit(Runnable task) {
        try {
            taskBuffer.put(task);
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }

    @Override
    public void start() {
        for (int i = 0; i < poolSize; i++) {
            var thread = new Thread(() -> {
                while (true) {
                    try {
                        taskBuffer.get().run();
                    } catch (InterruptedException ie) {
                        throw new RuntimeException(ie);
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }
}
