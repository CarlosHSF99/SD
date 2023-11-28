package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AuthReply extends Message {
    private final boolean success;

    public AuthReply(boolean success) {
        super(Type.AUTH_REPLY);
        this.success = success;
    }

    public boolean getSuccess() {
        return success;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeBoolean(success);
    }

    public static AuthReply deserialize(DataInputStream in) throws IOException {
        boolean success = in.readBoolean();
        return new AuthReply(success);
    }
}
