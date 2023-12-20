package connection.messages;

import connection.utils.Message;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record AuthRequest(String username, String password) implements Message {

    public static AuthRequest deserialize(DataInputStream in) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();
        return new AuthRequest(username, password);
    }

    @Override
    public Type type() {
        return Type.AUTH_REQUEST;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(username);
        out.writeUTF(password);
    }
}
