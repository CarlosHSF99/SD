package server;

public class Status {
    private final int memoryCapacity;
    private int memoryInUse = 0;
    private int pendingJobs = 0;


    public Status(int memoryCapacity) {
        this.memoryCapacity = memoryCapacity;
    }

    public void addJob(byte[] job) {
        if (memoryInUse + job.length > memoryCapacity) {
        }
        else {
            memoryInUse += job.length;
            pendingJobs++;
        }
    }

    public void removeJob(byte[] job) {
        memoryInUse -= job.length;
        pendingJobs--;
    }
}
