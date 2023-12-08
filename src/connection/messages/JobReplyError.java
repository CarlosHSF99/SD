package connection.messages;

import connection.utils.Payload;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record JobReplyError(int code, String message) implements Payload {

    public static JobReplyError deserialize(DataInputStream in) throws IOException {
        int code = in.readInt();
        String message = in.readUTF();
        return new JobReplyError(code, message);
    }

    @Override
    public Type getType() {
        return Type.JOB_REPLY_ERROR;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(code);
        out.writeUTF(message);
    }
}
