package connection.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record Message(Type type, Payload payload) {

    public Message(Payload payload) {
        this(payload.getType(), payload);
    }

    public static Message receive(DataInputStream in) throws IOException {
        var type = Type.deserialize(in);
        return new Message(type, type.deserializePayload(in));
    }

    public void send(DataOutputStream out) throws IOException {
        type.serialize(out);
        payload.serialize(out);
        out.flush();
    }
}
