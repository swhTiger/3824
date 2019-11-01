package com.swh.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 客户端Player对象，负责与服务器的连接以及通信
 * */
public class Player {

    private String name;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void connect(String IPAddress, int port) throws IOException {
        Socket socket = new Socket(IPAddress, port);    //通过IP地址和端口创建和服务器的连接
        this.printWriter = new PrintWriter(socket.getOutputStream(), true);    //自动刷新
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        send(name); //向服务器发送自己的名称
    }

    /*给服务器发送消息*/
    public void send(String s) {
        printWriter.println(s);
    }

    /*接收服务器发来的消息， 会阻塞*/
    public String read() throws IOException {
        return bufferedReader.readLine();
    }
}
