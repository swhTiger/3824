import com.swh.Cards;
import com.swh.client.Player;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class gameFrame {
    private JFrame frame;
    private JTextField answerTextField;
    private JButton clearButton;
    private JButton postButton;
    private JPanel rootPanel;
    private JLabel first;
    private JLabel second;
    private JLabel third;
    private JLabel fourth;
    private JLabel cardLabel1;
    private JLabel cardLabel2;
    private JLabel cardLabel3;
    private JLabel cardLabel4;
    private JLabel restTimeLabel;
    private JLabel roundLabel;

    private Player player;
    private Thread recThread;
    private Thread timerThread;
    private int restTime;

    private String[] cardGroup = new String[4];

    gameFrame(Player player) {
        frame = new JFrame("24点牌戏  "+player.getName());
        frame.setContentPane(rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(500,400));
        frame.setLocation(400, 200);
        frame.pack();
        frame.setVisible(true);
        this.player = player;
        recThread = new Thread(() -> {
            try {
                gameLogical();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        timerThread = new Thread(() -> {
            while (true) {
                restTimeLabel.setText(restTime+"");
                restTime--;
                if (restTime < 0) restTime = 0;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        clearButton.addActionListener(e -> answerTextField.setText(""));
        postButton.addActionListener(e -> {
           boolean result = Cards.checkAnswer(answerTextField.getText(), cardGroup);
           player.send(result+"");
           postButton.setEnabled(false);
        });
    }

    void Ready() {
        recThread.start();
        timerThread.start();
    }


    private void showScores(String[] names, int[] scores) {
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
        for (int i = 0; i < count; i++) {
            switch (i) {
                case 0:
                    first.setText("1. "+names[i]+" "+scores[i]);
                    break;
                case 1:
                    second.setText("2. "+names[i]+" "+scores[i]);
                    break;
                case 2:
                    third.setText("3. "+names[i]+" "+scores[i]);
                    break;
                case 4:
                    fourth.setText("4. "+names[i]+" "+scores[i]);
                    break;
            }
        }
    }

    private void gameLogical() throws IOException {
        while (true) {
            String msg = player.read();
            switch (msg) {
                case "Round":   //接收到服务器发来新的回合
                    String round = player.read();
                    roundLabel.setText("回合 "+round+"/13");
                    cardGroup[0] = player.read();
                    cardGroup[1] = player.read();
                    cardGroup[2] = player.read();
                    cardGroup[3] = player.read();
                    restTime = 100;
                    cardLabel1.setText(cardGroup[0]);
                    cardLabel2.setText(cardGroup[1]);
                    cardLabel3.setText(cardGroup[2]);
                    cardLabel4.setText(cardGroup[3]);
                    postButton.setEnabled(true);  //激活提交按钮
                    answerTextField.setText("");    //清空答题框
                    break;
                case "Score":   //接收到服务器发来所有玩家的分数
                    int count = Integer.parseInt(player.read());
                    String[] names = new String[count];
                    int[] scores = new int[count];
                    for (int i=0; i<count; i++) {
                        names[i] = player.read();
                        scores[i] = Integer.parseInt(player.read());
                    }
                    showScores(names, scores);
                    break;
                case "over":    //接收到服务器发送的游戏结束标志
                    player.send("over");    //同样返回一个结束标志，以结束服务器端的循环接收线程
                    return; //结束当前循环接收线程
            }
        }
    }

}
