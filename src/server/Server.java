package server;

import concurrentUtils.BoundedBuffer;
import concurrentUtils.ThreadPool;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) throws IOException {
        var auth = new Auth();
        var scheduler = new MasterScheduler(Integer.parseInt(args[0]));
        var taskBuffer = new BoundedBuffer<Runnable>(1024);
        var threadPool = new ThreadPool(8, taskBuffer);
        threadPool.start();
        try (var serverSocket = new ServerSocket(1337)) {
            while (true) {
                var socket = serverSocket.accept();
                var session = new Thread(new Session(socket, taskBuffer, auth, scheduler));
                session.start();
            }
        }
    }
}
