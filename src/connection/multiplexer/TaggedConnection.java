package connection.multiplexer;

import connection.utils.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable {
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final Lock lockSend = new ReentrantLock();
    private final Lock lockReceive = new ReentrantLock();

    public TaggedConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void send(Frame frame) throws IOException {
        lockSend.lock();
        try {
            frame.serialize(outputStream);
            outputStream.flush();
        } finally {
            lockSend.unlock();
        }
    }

    public void send(int tag, Message message) throws IOException {
        send(new Frame(tag, message));
    }

    public Frame receive() throws IOException {
        lockReceive.lock();
        try {
            return Frame.deserialize(inputStream);
        } finally {
            lockReceive.unlock();
        }
    }

    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
        socket.close();
    }
}
