package messages;

import messages.utils.Message;
import messages.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record WorkerHandshake(int memory) implements Message {

    public static WorkerHandshake deserialize(DataInputStream in) throws IOException {
        int memory = in.readInt();
        return new WorkerHandshake(memory);
    }

    @Override
    public Type type() {
        return Type.WORKER_HANDSHAKE;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(memory);
    }
}
