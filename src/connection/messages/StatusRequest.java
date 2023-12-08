package connection.messages;

import connection.utils.Payload;
import connection.utils.Type;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public record StatusRequest() implements Payload {

    public static StatusRequest deserialize(DataInputStream in) throws IOException {
        return new StatusRequest();
    }

    @Override
    public Type getType() {
        return Type.STATUS_REQUEST;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
    }
}
