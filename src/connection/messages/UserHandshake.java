package connection.messages;

import connection.utils.Message;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record UserHandshake(String username, String password) implements Message {

    public static UserHandshake deserialize(DataInputStream in) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();
        return new UserHandshake(username, password);
    }

    @Override
    public Type type() {
        return Type.USER_HANDSHAKE;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(username);
        out.writeUTF(password);
    }
}
