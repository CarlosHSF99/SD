package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record JobRequest(byte[] code) implements Serializable {

    public static JobRequest deserialize(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        return new JobRequest(data);
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        Type.JOB_REQUEST.serialize(out);
        out.writeInt(code.length);
        out.write(code);
    }
}
