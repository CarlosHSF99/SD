package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public enum Type implements Serializable {
    AUTH_REQUEST,
    AUTH_REPLY,
    JOB_REQUEST,
    JOB_REPLY_OK,
    JOB_REPLY_ERROR,
    STATUS_REQUEST,
    STATUS_REPLY;

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeChar(ordinal());
    }

    public static Type deserialize(DataInputStream in) throws IOException {
        return Type.values()[in.readChar()];
    }
}
