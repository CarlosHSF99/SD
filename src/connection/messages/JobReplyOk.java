package connection.messages;

import connection.utils.Message;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record JobReplyOk(byte[] output) implements Message {

    public JobReplyOk(byte[] output) {
        this.output = output.clone();
    }

    public byte[] output() {
        return output.clone();
    }

    public static JobReplyOk deserialize(DataInputStream in) throws IOException {
        int outputLength = in.readInt();
        byte[] output = new byte[outputLength];
        in.readFully(output);
        return new JobReplyOk(output);
    }

    @Override
    public Type type() {
        return Type.JOB_REPLY_OK;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(output.length);
        out.write(output);
    }
}
