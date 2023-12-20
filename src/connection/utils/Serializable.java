package connection.utils;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Serializable {
    void serialize(DataOutputStream out) throws IOException;
}
