package com.jsteamkit.cm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class InitialCMServer extends CMServer {

    public long lastPing = 0L;

    public InitialCMServer(String ip, int port) {
        super(ip, port);

        try {
            long startTime = System.nanoTime();

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 1000);
            socket.close();

            lastPing = System.nanoTime() - startTime;
        } catch (IOException e) {
            lastPing = -1L;
        }
    }
}
