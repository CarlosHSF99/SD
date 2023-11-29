package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record JobReplyError(int code, String message) implements Serializable {

    public static JobReplyError deserialize(DataInputStream in) throws IOException {
        int code = in.readInt();
        String message = in.readUTF();
        return new JobReplyError(code, message);
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        Type.JOB_REPLY_ERROR.serialize(out);
        out.writeInt(code);
        out.writeUTF(message);
    }
}
