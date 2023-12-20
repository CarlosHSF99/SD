package connection.multiplexer;

import connection.utils.Message;
import connection.utils.Serializable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record Frame(int tag, Message message) implements Serializable {
    @Override
    public void serialize(DataOutputStream outputStream) throws IOException {
        outputStream.writeInt(tag);
        message.serialize(outputStream);
    }

    public static Frame deserialize(DataInputStream inputStream) throws IOException {
        return new Frame(inputStream.readInt(), Message.deserialize(inputStream));
    }
}
