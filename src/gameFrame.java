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
    private JLabel resultLabel;

    private Player player;
    private Thread recThread;
    private Thread timerThread;
    private int restTime;

    private String[] cardGroup = new String[4];

    gameFrame(Player player) {
        frame = new JFrame("24点牌戏  " + player.getName());
        frame.setContentPane(rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(500,400));
        frame.setLocation(400, 200);
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
        clearButton.addActionListener(e -> answerTextField.setText(""));    //使用lambda表达式
        postButton.addActionListener(e -> {
           boolean result = Cards.checkAnswer(answerTextField.getText(), cardGroup);
           //显示结果信息
           if (result)
               resultLabel.setText("正确！");
           else
               resultLabel.setText("错误！");
           player.send(result+"");  //将处理的答案结果发送给服务器，由服务器统计分数
           postButton.setEnabled(false);    //只能提交一次，所以暂时禁用提交按钮
        });
    }

    void Ready() {
        recThread.start();
        timerThread.start();
    }


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
                case 3:
                    fourth.setText("4. "+names[i]+" "+scores[i]);
                    break;
            }
        }
    }

    private void gameLogical() throws IOException {
        int count = Integer.parseInt(player.read());
        String[] names = new String[count];
        int[] scores = new int[count];
        while (true) {
            String msg = player.read();
            switch (msg) {
                case "Round":   //接收到服务器发来新的回合
                    String round = player.read();   //接收回合数
                    roundLabel.setText("回合 "+round+"/13");  //显示回合数
                    //接收4张卡牌
                    cardGroup[0] = player.read();
                    cardGroup[1] = player.read();
                    cardGroup[2] = player.read();
                    cardGroup[3] = player.read();
                    restTime = 100; //重置时间
                    //显示4张卡牌
                    cardLabel1.setText(cardGroup[0]);
                    cardLabel2.setText(cardGroup[1]);
                    cardLabel3.setText(cardGroup[2]);
                    cardLabel4.setText(cardGroup[3]);
                    postButton.setEnabled(true);  //重新激活提交按钮
                    answerTextField.setText("");    //清空答题框
                    resultLabel.setText("");    //清空结果提示
                    break;
                case "Score":   //接收到服务器发来所有玩家的分数
                    //int count = Integer.parseInt(player.read());    //接收数量信息
                    //依次接收玩家名称和分数

                    for (int i=0; i<count; i++) {
                        names[i] = player.read();
                        scores[i] = Integer.parseInt(player.read());
                    }
                    showScores(names, scores);  //排序并显示
                    break;
                case "over":    //接收到服务器发送的游戏结束标志
                    player.send("over");    //同样返回一个结束标志，以结束服务器端的循环接收线程
                    for (int i = 0; i < count; i++) {
                        if (names[i].equals(player.getName())) {
                            JOptionPane.showMessageDialog(frame, String.format("您获得了第% d 名！", i+1), "结果", JOptionPane.INFORMATION_MESSAGE);
                            break;
                        }
                    }
                    System.exit(1);
                    //return; //结束当前循环接收线程
            }
        }
    }

}
