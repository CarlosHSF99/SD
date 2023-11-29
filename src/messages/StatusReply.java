package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record StatusReply(int availableMemory, int pendingTasks) implements Serializable {

    public static StatusReply deserialize(DataInputStream in) throws IOException {
        int availableMemory = in.readInt();
        int pendingTasks = in.readInt();
        return new StatusReply(availableMemory, pendingTasks);
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        Type.STATUS_REPLY.serialize(out);
        out.writeInt(availableMemory);
        out.writeInt(pendingTasks);
    }
}
