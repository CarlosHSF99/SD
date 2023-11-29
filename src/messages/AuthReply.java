package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record AuthReply(boolean success) implements Serializable {

    public static AuthReply deserialize(DataInputStream in) throws IOException {
        boolean success = in.readBoolean();
        return new AuthReply(success);
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        Type.AUTH_REPLY.serialize(out);
        out.writeBoolean(success);
    }
}
