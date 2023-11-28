package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class JobReplyError extends Message {
    private final int code;
    private final String message;

    public JobReplyError(int code, String message) {
        super(Type.JOB_REPLY_ERROR);
        this.code = code;
        this.message = message;
    }

    public static JobReplyError deserialize(DataInputStream in) throws IOException {
        int code = in.readInt();
        String message = in.readUTF();
        return new JobReplyError(code, message);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeInt(code);
        out.writeUTF(message);
    }
}
