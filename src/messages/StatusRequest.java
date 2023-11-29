package messages;

import java.io.DataOutputStream;
import java.io.IOException;

public record StatusRequest() implements Serializable {
    @Override
    public void serialize(DataOutputStream out) throws IOException {
        Type.STATUS_REQUEST.serialize(out);
    }
}
