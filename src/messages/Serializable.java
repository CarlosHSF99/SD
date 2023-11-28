package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface Serializable {
    void serialize(DataOutputStream out) throws IOException;
}
