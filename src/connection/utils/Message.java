package connection.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Message extends Serializable {
    static Message deserialize(DataInputStream stream) throws IOException {
        return Type.deserialize(stream).deserializeMessage(stream);
    }

    Type type();

    @Override
    default void serialize(DataOutputStream stream) throws IOException {
        type().serialize(stream);
    }
}
