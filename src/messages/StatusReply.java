package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StatusReply extends Message {
    private final int availableMemory;
    private final int pendingTasks;

    public StatusReply(int availableMemory, int pendingTasks) {
        super(Type.STATUS_REPLY);
        this.availableMemory = availableMemory;
        this.pendingTasks = pendingTasks;
    }

    public static StatusReply deserialize(DataInputStream in) throws IOException {
        int availableMemory = in.readInt();
        int pendingTasks = in.readInt();
        return new StatusReply(availableMemory, pendingTasks);
    }

    public int getAvailableMemory() {
        return availableMemory;
    }

    public int getPendingTasks() {
        return pendingTasks;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeInt(availableMemory);
        out.writeInt(pendingTasks);
    }
}
