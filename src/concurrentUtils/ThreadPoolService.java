package concurrentUtils;

public interface ThreadPoolService {
    void submit(Runnable task);

    void start();
}
