package connection.messages;

import connection.utils.Message;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record AuthReply(boolean success) implements Message {

    public static AuthReply deserialize(DataInputStream in) throws IOException {
        boolean success = in.readBoolean();
        return new AuthReply(success);
    }

    @Override
    public Type type() {
        return Type.AUTH_REPLY;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeBoolean(success);
    }
}
