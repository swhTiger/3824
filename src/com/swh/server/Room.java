package com.swh.server;

import com.swh.Cards;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Enumeration;


public class Room extends Thread {
    private ServerSocket serverSocket;
    private String IPAddress;
    private int count;
    private Player[] players;

    public Room(int port, int count) throws IOException {
        this.serverSocket = new ServerSocket(port, count);
        //IPAddress = InetAddress.getLocalHost().getHostAddress();
        IPAddress = getHostIp();
        System.out.println("Room Server创建成功");
        this.count = count;
        this.players = new Player[count];
        start();
    }

    public String getIPAddress() {
        return IPAddress;
    }

    private static String getHostIp(){
        try{
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()){
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()){
                    InetAddress ip = (InetAddress) addresses.nextElement();
                    if (ip instanceof Inet4Address
                            && !ip.isLoopbackAddress() //loopback地址即本机地址，IPv4的loopback范围是127.0.0.0 ~ 127.255.255.255
                            && !ip.getHostAddress().contains(":")){
                        System.out.println("本机的IP = " + ip.getHostAddress());
                        return ip.getHostAddress();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


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
        System.out.println("开始等待玩家接入...");
        for (int n=0; n < count; n++) {
            try {
                players[n] = new Player(serverSocket.accept());
                System.out.println("服务端："+players[n].getName()+" 接入成功");
                p += players[n].getName() + " ";
                broadcast(p);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(2);
            }
        }
        //所有玩家都已加入，开始游戏
        broadcast("begin");
        gameStart();
    }

    private void gameStart() {
        Cards cards = new Cards();  //新建一副打乱的卡牌对象
        //让服务器的每个玩家开始监听从客户端接收的消息
        for (Player p : players) new Thread(() -> {
            try {
                p.clearScore();
                gameLogical(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        String[] group;
        int round = 1;  //回合数
        //先发送一次玩家的顺序
        broadcast("Score");
        broadcast(count+"");
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

            Player.answeredPlayerCount = 0; //将以回答人数置零
            //给所有玩家发送各个玩家的分数
            broadcast("Score");
            broadcast(count+"");
            for (Player p : players) {
                broadcast(p.getName());
                broadcast(p.getScore()+"");
            }
            round++;    //回合数+1
        }
        broadcast("over");
    }

    private void gameLogical(Player player) throws IOException {
        while (true) {
            String msg = player.read();
            switch (msg) {
                case "true":
                    player.winScore(4-Player.answeredPlayerCount);
                    Player.answeredPlayerCount++;
                    broadcast("Score");
                    broadcast(count+"");
                    for (Player p : players) {
                        broadcast(p.getName());
                        broadcast(p.getScore()+"");
                    }
                    break;
                case "false":
                    Player.answeredPlayerCount++;
                    break;
                case "over": return;
            }
        }
    }

}
