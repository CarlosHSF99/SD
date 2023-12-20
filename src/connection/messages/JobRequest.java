package connection.messages;

import connection.utils.Message;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record JobRequest(byte[] code, int memory) implements Message {

    public static JobRequest deserialize(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        int memory = in.readInt();
        return new JobRequest(data, memory);
    }

    @Override
    public Type type() {
        return Type.JOB_REQUEST;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        Message.super.serialize(out);
        out.writeInt(code.length);
        out.write(code);
        out.writeInt(memory);
    }
}
