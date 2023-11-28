package messages;

import java.io.DataOutputStream;
import java.io.IOException;

public class JobReplyOk extends Message {
    private final byte[] output;

    public JobReplyOk(byte[] output) {
        super(Type.JOB_REPLY_OK);
        this.output = output;
    }

    public byte[] getOutput() {
        return output;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeInt(output.length);
        out.write(output);
    }
}
