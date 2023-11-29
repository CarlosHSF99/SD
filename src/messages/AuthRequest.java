package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record AuthRequest(String username, String password) implements Serializable {

    public static AuthRequest deserialize(DataInputStream in) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();
        return new AuthRequest(username, password);
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        Type.AUTH_REQUEST.serialize(out);
        out.writeUTF(username);
        out.writeUTF(password);
    }
}
