package com.swh.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Player {

    private String name;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    static int answeredPlayerCount = 0;

    int getScore() {
        return score;
    }

    void winScore(int score) {
        this.score += score;
    }

    void clearScore() {
        this.score = 0;
    }

    private int score;

    Player(Socket socket) throws IOException {
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.name = bufferedReader.readLine();
        score = 0;
    }

    String getName() {
        return name;
    }

    //从服务端发送消息给客户端
    void send(String msg) {
        printWriter.println(msg);
    }

    //从客户端接收消息，给外部调用的
    String read() throws IOException {
        return bufferedReader.readLine();
    }
}
