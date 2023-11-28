package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Message implements Serializable {
    private final Type type;

    public Message(Type type) {
        this.type = type;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeChar(type.ordinal());
    }

    public static Type getType(DataInputStream in) throws IOException {
        return Type.values()[in.readChar()];
    }
}
