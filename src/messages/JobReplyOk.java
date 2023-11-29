package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record JobReplyOk(byte[] output) implements Serializable {

    public static JobReplyOk deserialize(DataInputStream in) throws IOException {
        int outputLength = in.readInt();
        byte[] output = new byte[outputLength];
        in.readFully(output);
        return new JobReplyOk(output);
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        Type.JOB_REPLY_OK.serialize(out);
        out.writeInt(output.length);
        out.write(output);
    }
}