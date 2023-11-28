package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class JobRequest extends Message {
    private final byte[] code;

    public JobRequest(byte[] code) {
        super(Type.JOB_REQUEST);
        this.code = code;
    }

    public byte[] getCode() {
        return code;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeInt(code.length);
        out.write(code);
    }

    public static JobRequest deserialize(DataInputStream in) throws IOException {
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        return new JobRequest(data);
    }
}
