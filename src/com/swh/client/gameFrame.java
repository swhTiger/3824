package com.swh.client;

import com.swh.server.Cards;
import fonts.MyFont;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class gameFrame {
    private JFrame frame;
    private JTextField answerTextField;
    private JButton clearButton;
    private JButton postButton;
    private JPanel rootPanel;
    private JLabel nameLabel1;
    private JLabel nameLabel2;
    private JLabel nameLabel3;
    private JLabel nameLabel4;
    private JLabel cardLabel1;
    private JLabel cardLabel2;
    private JLabel cardLabel3;
    private JLabel cardLabel4;
    private JLabel restTimeLabel;
    private JLabel roundLabel;
    private JLabel resultLabel;
    private JLabel scoreLabel1;
    private JLabel scoreLabel2;
    private JLabel scoreLabel3;
    private JLabel scoreLabel4;
    private JLabel rankLabel1;
    private JLabel rankLabel2;
    private JLabel rankLabel3;
    private JLabel rankLabel4;
    private JLabel rankText;

    private Player player;  // client/Player
    private Thread recThread;
    private Thread timerThread;
    private int restTime;
    private boolean answered = false;
    boolean gameOver = false;

    private String[] cardGroup = new String[4];

    gameFrame(Player player) {
        Font font = MyFont.getFont(0, 32);
        rankText.setFont(font);
        roundLabel.setFont(font);
        restTimeLabel.setFont(font);
        font = MyFont.getFont(0, 20);
        nameLabel1.setFont(font);
        nameLabel2.setFont(font);
        nameLabel3.setFont(font);
        nameLabel4.setFont(font);
        rankLabel1.setFont(font);
        rankLabel2.setFont(font);
        rankLabel3.setFont(font);
        rankLabel4.setFont(font);
        scoreLabel1.setFont(font);
        scoreLabel2.setFont(font);
        scoreLabel3.setFont(font);
        scoreLabel4.setFont(font);
        answerTextField.setFont(font);
        font = MyFont.getFont(0, 18);
        clearButton.setFont(font);
        postButton.setFont(font);

        frame = new JFrame("24点牌戏  " + player.getName());
        frame.setIconImage(new ImageIcon(getClass().getResource("/images/Icon.png")).getImage());
        frame.setContentPane(rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(650,480));
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
        this.player = player;
        //创建接收线程对象(此时还没开启线程)，处理服务器发送的消息
        recThread = new Thread(() -> {
            try {
                gameLogical();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //倒计时器线程
        timerThread = new Thread(() -> {
            while (!gameOver) {
                if (restTime == 10) restTimeLabel.setForeground(Color.RED);
                else if (restTime == 100) restTimeLabel.setForeground(Color.BLACK);
                restTimeLabel.setText(String.format("%d", restTime));
                restTime--;
                if (restTime < 0) restTime = 0;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        clearButton.addActionListener(e -> answerTextField.setText(""));    //使用lambda表达式
        postButton.addActionListener(e -> postAnswer());
        answerTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) postAnswer();
            }
        });
    }

    /**
     * 启动客户端的接收线程和倒计时线程
     * */
    void Ready() {
        recThread.start();
        timerThread.start();
    }

    private void postAnswer() {
        if (answered) return;
        boolean result = Cards.checkAnswer(answerTextField.getText(), cardGroup);    //验证答案
        //显示结果信息
        if (result)
            resultLabel.setText("正确！");
        else
            resultLabel.setText("错误！");
        player.send(result+"");  //将处理的答案结果发送给服务器，由服务器统计分数
        postButton.setEnabled(false);    //只能提交一次，所以暂时禁用提交按钮
        answered = true;
    }

    void destroy() {
        frame.dispose();
    }

    /**
     * 对玩家进行排名，并显示排名和分数
     * @param names : 玩家名称
     * @param scores : 与名称依次对应的玩家分数
     * */
    private void showScores(String[] names, int[] scores) {
        //先降序排序，再显示排名和分数
        String name;
        int score;
        int count = names.length;
        for (int i = 0; i < count-1; i++) {
            for (int j = 0; j < count-1-i ; j++) {
                if (scores[j] < scores[j+1]) {
                    name = names[j];
                    score = scores[j];
                    names[j] = names[j+1];
                    scores[j] = scores[j+1];
                    names[j+1] = name;
                    scores[j+1] = score;
                }
            }
        }
        try {
            nameLabel1.setText(names[0]);
            scoreLabel1.setText(String.valueOf(scores[0]));
            nameLabel2.setText(names[1]);
            scoreLabel2.setText(String.valueOf(scores[1]));
            nameLabel3.setText(names[2]);
            scoreLabel3.setText(String.valueOf(scores[2]));
            nameLabel4.setText(names[3]);
            scoreLabel4.setText(String.valueOf(scores[3]));
        } catch (ArrayIndexOutOfBoundsException ignored) {}
    }

    /**
     * 循环接收服务器的消息并处理，直到接收到“over”为止
     * */
    private void gameLogical() throws IOException {
        int count = Integer.parseInt(player.read());
        if (count == 3)
            rankLabel3.setText("3.");
        else if (count == 4) {
            rankLabel3.setText("3.");
            rankLabel4.setText("4.");
        }

        String[] names = new String[count];
        int[] scores = new int[count];
        while (true) {
            String msg = player.read();
            switch (msg) {
                case "Round":   //接收到服务器发来新的回合
                    String round = player.read();   //接收回合数
                    roundLabel.setText("回合 "+round+"/13");  //显示回合数
                    restTime = 100; //重置时间
                    //接收4张卡牌
                    String[] cards = new String[4];
                    for (int i = 0; i < 4; i++) {
                        cards[i] = player.read();
                        cardGroup[i] = cards[i].split("_")[1];
                    }
                    //显示4张卡牌
                    cardLabel1.setIcon(new ImageIcon(getClass().getResource("/images/pkp/"+cards[0]+".jpg")));
                    cardLabel2.setIcon(new ImageIcon(getClass().getResource("/images/pkp/"+cards[1]+".jpg")));
                    cardLabel3.setIcon(new ImageIcon(getClass().getResource("/images/pkp/"+cards[2]+".jpg")));
                    cardLabel4.setIcon(new ImageIcon(getClass().getResource("/images/pkp/"+cards[3]+".jpg")));
                    postButton.setEnabled(true);  //重新激活提交按钮
                    answered = false;   //设置为未回答
                    answerTextField.setText("");    //清空答题框
                    resultLabel.setText("");    //清空结果提示
                    break;
                case "Score":   //接收到服务器发来所有玩家的分数
                    //依次接收玩家名称和分数
                    for (int i=0; i<count; i++) {
                        names[i] = player.read();
                        scores[i] = Integer.parseInt(player.read());
                    }
                    showScores(names, scores);  //排序并显示
                    break;
                case "over":    //接收到服务器发送的游戏结束标志
                    player.send("over");    //同样返回一个结束标志，以结束服务器端的循环接收线程
                    restTime = 0;
                    player.disconnect();
                    for (int i = 0; i < count; i++) {
                        if (names[i].equals(player.getName())) {
                            JOptionPane.showMessageDialog(frame, String.format("您获得了第% d 名！", i+1), "结果", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                    }
                    gameOver = true;    //游戏结束标志
                    return;
            }
        }
    }

}
