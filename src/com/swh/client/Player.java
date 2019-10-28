package com.swh.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Player {

    public String getName() {
        return name;
    }

    private String name;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    public Player(String name) {
        this.name = name;
    }

    public void connect(String IPAddress, int port) throws IOException {
        //Socket socket = new Socket("127.0.0.1", port);
        Socket socket = new Socket(IPAddress, port);
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        send(name);
    }

    public void send(String s) {
        printWriter.println(s);
    }

    public String read() throws IOException {
        return bufferedReader.readLine();
    }
}
