package connection.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Connection implements AutoCloseable {
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public Message receive() throws IOException {
        return Type.deserialize(inputStream).deserializeMessage(inputStream);
    }

    public void send(Message message) throws IOException {
        message.type().serialize(outputStream);
        message.serialize(outputStream);
        outputStream.flush();
    }

    @Override
    public void close() throws Exception {
        socket.close();
        inputStream.close();
        outputStream.close();
    }
}
