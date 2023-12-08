package connection.utils;

import java.io.DataOutputStream;
import java.io.IOException;

public interface Payload extends Serializable {
    Type getType();

}
