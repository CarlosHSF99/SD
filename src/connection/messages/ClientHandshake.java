package connection.messages;

import connection.utils.Message;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record ClientHandshake() implements Message {

    public static ClientHandshake deserialize(DataInputStream in) throws IOException {
        return new ClientHandshake();
    }

    @Override
    public Type type() {
        return Type.CLIENT_HANDSHAKE;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        Message.super.serialize(out);
    }
}
