package messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AuthRequest extends Message {
    private final String username;
    private final String password;

    public AuthRequest(String username, String password) {
        super(Type.AUTH_REQUEST);
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public void serialize(DataOutputStream out) throws IOException {
        super.serialize(out);
        out.writeUTF(username);
        out.writeUTF(password);
    }

    public static AuthRequest deserialize(DataInputStream in) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();
        return new AuthRequest(username, password);
    }
}
