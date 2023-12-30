package faas;

public record Status(int availableMemory, int maxJobMemory, int pendingJobs) {
}
