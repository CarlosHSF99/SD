package messages;

import messages.utils.Message;
import messages.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record StatusRequest() implements Message {

    public static StatusRequest deserialize(DataInputStream in) throws IOException {
        return new StatusRequest();
    }

    @Override
    public Type type() {
        return Type.STATUS_REQUEST;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
    }
}
