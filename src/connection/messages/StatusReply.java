package connection.messages;

import connection.utils.Message;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record StatusReply(int availableMemory, int pendingTasks) implements Message {

    public static StatusReply deserialize(DataInputStream in) throws IOException {
        int availableMemory = in.readInt();
        int pendingTasks = in.readInt();
        return new StatusReply(availableMemory, pendingTasks);
    }

    @Override
    public Type getType() {
        return Type.STATUS_REPLY;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(availableMemory);
        out.writeInt(pendingTasks);
    }
}
