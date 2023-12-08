package connection.messages;

import connection.utils.Message;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record JobRequest(byte[] code) implements Message {

    public static JobRequest deserialize(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        return new JobRequest(data);
    }

    @Override
    public Type getType() {
        return Type.JOB_REQUEST;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(code.length);
        out.write(code);
    }
}
