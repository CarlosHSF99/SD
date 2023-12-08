package connection.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record Connection(Message message) {

    public static Message receive(DataInputStream in) throws IOException {
        return Type.deserialize(in).deserializePayload(in);
    }

    public void send(DataOutputStream out) throws IOException {
        message.getType().serialize(out);
        message.serialize(out);
        out.flush();
    }
}
