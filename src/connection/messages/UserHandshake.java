package connection.messages;

import connection.utils.Message;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record ClientHandshake(String username, String password) implements Message {

    public static ClientHandshake deserialize(DataInputStream in) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();
        return new ClientHandshake(username, password);
    }

    @Override
    public Type type() {
        return Type.CLIENT_HANDSHAKE;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        Message.super.serialize(out);
        out.writeUTF(username);
        out.writeUTF(password);
    }
}
