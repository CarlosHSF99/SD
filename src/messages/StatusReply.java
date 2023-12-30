package messages;

import messages.utils.Message;
import messages.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record StatusReply(int availableMemory, int maxJobMemory, int pendingJobs) implements Message {

    public static StatusReply deserialize(DataInputStream in) throws IOException {
        int availableMemory = in.readInt();
        int maxJobMemory = in.readInt();
        int pendingTasks = in.readInt();
        return new StatusReply(availableMemory, maxJobMemory, pendingTasks);
    }

    @Override
    public Type type() {
        return Type.STATUS_REPLY;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(availableMemory);
        out.writeInt(maxJobMemory);
        out.writeInt(pendingJobs);
    }
}
