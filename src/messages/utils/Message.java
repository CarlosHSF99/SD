package messages.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Message extends Serializable {

    static Message deserializeMessage(DataInputStream stream) throws IOException {
        return Type.deserialize(stream).deserializePayload(stream);
    }

    Type type();

    default void serializeMessage(DataOutputStream stream) throws IOException {
        type().serialize(stream);
        serialize(stream);
    }
}
