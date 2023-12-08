package connection.messages;

import connection.utils.Payload;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record AuthReply(boolean success) implements Payload {

    public static AuthReply deserialize(DataInputStream in) throws IOException {
        boolean success = in.readBoolean();
        return new AuthReply(success);
    }

    @Override
    public Type getType() {
        return Type.AUTH_REPLY;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeBoolean(success);
    }
}
