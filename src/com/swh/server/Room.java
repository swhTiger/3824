package com.swh.server;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Enumeration;


public class Room extends Thread {
    private ServerSocket serverSocket;
    private String IPAddress;
    private int count;  //玩家数量
    private Player[] players;

    /**
     * 创建一个房间对象
     * @param port : 端口号
     * @param count ：玩家人数
     * */
    public Room(int port, int count) throws IOException {
        this.serverSocket = new ServerSocket(port, count);
        IPAddress = getHostIp();
        this.count = count;
        this.players = new Player[count];
        start();
    }

    public String getIPAddress() {
        return IPAddress;
    }

    /**
     * 获取本机的内网IP地址
     * */
    private static String getHostIp() {
        try{
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip instanceof Inet4Address
                            && !ip.isLoopbackAddress() //loopback地址即本机地址，IPv4的loopback范围是127.0.0.0 ~ 127.255.255.255
                            && !ip.getHostAddress().contains(":")) {
                        return ip.getHostAddress();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将消息发送给每一位连接的玩家
     * @param msg : 要发送的消息(指令、标志、数据)
     * */
    private void broadcast(String msg) {
        for (int i=0; i<count; i++) {
            if (players[i] != null)
                players[i].send(msg);
        }
    }

    @Override
    public void run() {
        //等待玩家加入房间
        String p = "";
        for (int n=0; n < count; n++) {
            try {
                players[n] = new Player(serverSocket.accept());
                p += players[n].getName() + " ";
                broadcast(p);
            } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(2);
            }
        }
        //所有玩家都已加入，开始游戏
        broadcast("begin"); //发出通知，使客户端进入游戏界面
        gameStart();
    }

    /**
     * 服务器端的主要处理函数，给客户端发送指令和数据
     * */
    private void gameStart() {
        Cards cards = new Cards();  //新建一副打乱的卡牌对象
        //让服务器的每个玩家对象开始监听从客户端接收的消息
        for (Player p : players) new Thread(() -> {
            try {
                p.clearScore();
                gameLogical(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        String[] group; //一组卡牌
        int round = 1;  //回合数
        //先发送一次玩家的顺序
        broadcast(count+"");
        broadcast("Score");
        for (Player p : players) {
            broadcast(p.getName());
            broadcast(p.getScore()+"");
        }
        //游戏逻辑主循环
        while ((group=cards.next()) != null) {
            broadcast("Round");  //发送回合数
            broadcast(round+"");
            //发送卡牌
            for (String card : group) {
                broadcast(card);
            }
            //倒计时100秒，或者所有人都已经回答了就跳出循环
            int t = 100;
            while (t >= 0) {
                if (Player.answeredPlayerCount == count)
                    break;
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                t--;
            }

            Player.answeredPlayerCount = 0; //将已经回答人数置零
            Player.rightPlayerCount = 0;    //将回答正确人数置零
            //给所有玩家发送各个玩家的分数
            broadcast("Score");
            for (Player p : players) {
                broadcast(p.getName());
                broadcast(p.getScore()+"");
            }
            round++;    //回合数+1
        }
        broadcast("over");
    }

    /**
     * 处理客户端发送的消息，每一位玩家都需要开一个线程处理
     * */
    private void gameLogical(Player player) throws IOException {
        while (true) {
            String msg = player.read();
            switch (msg) {
                case "true":    //回答正确
                    player.winScore(4-Player.rightPlayerCount); //获得积分
                    Player.rightPlayerCount++;  //静态成员变量，回答正确人数+1
                    Player.answeredPlayerCount++;   //静态成员变量，回答人数+1
                    //向所有玩家发送当前的分数
                    broadcast("Score");
                    for (Player p : players) {
                        broadcast(p.getName());
                        broadcast(p.getScore()+"");
                    }
                    break;
                case "false":   //回答错误
                    Player.answeredPlayerCount++;   //回答人数+1
                    break;
                case "over": return;    //游戏结束
            }
        }
    }

}
