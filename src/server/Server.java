package server;

import concurrentUtils.GrowableThreadPoolService;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {
    public static void main(String[] args) throws IOException {
        var auth = new Auth();
        var scheduler = new MasterScheduler();
        var threadPool = new GrowableThreadPoolService(4);
        threadPool.start();
        try (var serverSocket = new ServerSocket(1337)) {
            while (true) {
                var socket = serverSocket.accept();
                var session = new Thread(new Session(socket, threadPool, auth, scheduler));
                session.start();
            }
        }
    }
}
