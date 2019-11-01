import com.swh.client.Player;
import com.swh.server.Room;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;


/**
 * rootPanel布局方式 : CardLayout
 * Card1 : choosePanel (选择创建房间或者加入房间)
 * Card2 : createPanel (创建房间界面)
 * Card3 : enterPanel (加入房间房间)
 * Card4 : waitPanel (等待其他玩家加入界面)
 * */
public class Launcher {
    private JFrame frame;

    private JPanel rootPanel;
    private JPanel choosePanel;
    private JPanel createPanel;
    private JPanel enterPanel;
    private JButton createRoomButton;
    private JButton enterRoomButton;
    private JLabel imageLabel;
    private JTextField textField1;
    private JButton returnButton;
    private JButton createButton;
    private JComboBox comboBox1;
    private JTextField portTextField;
    private JButton returnButton1;
    private JButton enterButton;
    private JPanel waitPanel;
    private JTextArea textArea1;
    private JTextField IPTextField;

    private Player player;

    private Launcher() {
        try {
            imageLabel.setIcon(new ImageIcon(getClass().getResource("assets/cardpicture.png")));//在打包为jar时获取包里面的资源
        } catch (NullPointerException e) {
            imageLabel.setIcon(new ImageIcon("assets/cardpicture.png"));
        }
        createRoomButton.addActionListener(e -> ((CardLayout) rootPanel.getLayout()).show(rootPanel, "Card2"));
        returnButton.addActionListener(e -> ((CardLayout) rootPanel.getLayout()).show(rootPanel, "Card1"));
        enterRoomButton.addActionListener(e -> ((CardLayout) rootPanel.getLayout()).show(rootPanel, "Card3"));
        returnButton1.addActionListener(e -> ((CardLayout) rootPanel.getLayout()).show(rootPanel, "Card1"));
        createButton.addActionListener(e -> new Thread(this::createRoom).start());

        frame = new JFrame("Launcher");
        frame.setContentPane(rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocation(400, 200);
        frame.pack();
        frame.setVisible(true);
        String name;
        while (true) {  //通过弹窗获取玩家名称
            name = JOptionPane.showInputDialog(frame.getContentPane(), "输入玩家名称", "创建玩家", JOptionPane.PLAIN_MESSAGE);
            if (name == null)   //如果点击的“取消”得到的为null，就关闭客服端
                System.exit(1);
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame.getContentPane(), "名称不能为空！", "提醒", JOptionPane.WARNING_MESSAGE);
            } else break;
        }
        frame.setTitle(name);   //重新将Title设置为玩家的名称
        player = new Player(name);  //创建 client/Player对象

        enterButton.addActionListener(e -> new Thread(this::enterRoom).start());
    }

    private void waitPlayers() {
        ((CardLayout) rootPanel.getLayout()).show(rootPanel, "Card4");  //显示waitPanel
        while (true) {
            try {
                String rec = player.read();
                if (rec.equals("begin")) {
                    frame.setVisible(false);    //游戏开始，隐藏Launcher
                    runGame();
                    return;
                } else {
                    //刷新人员列表
                    rec = rec.replaceAll(" ", "\n");
                    textArea1.setText("");
                    textArea1.append(rec+"\n");
                }
            } catch (IOException e) {
                System.out.println("消息接收时出现错误");
            }
        }

    }

    private void runGame() {
        gameFrame gameFrame = new gameFrame(player);    //开启游戏界面
        gameFrame.Ready();
    }

    private void enterRoom() {
        String ipAddress = IPTextField.getText();   //获取输入的IP地址
        int port = Integer.parseInt(portTextField.getText());   //获取输入的端口号
        try {
            player.connect(ipAddress, port);    //连接到服务器
            waitPlayers();  //进入等待玩家界面
        } catch (IOException e) {
            JOptionPane.showMessageDialog(rootPanel, "连接到房间失败","错误", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void createRoom() {
        int port;
        try {
            port = Integer.parseInt(textField1.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPanel, "非法端口号","错误", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (port <= 0) {
            JOptionPane.showMessageDialog(rootPanel, "换个房间号试试","错误", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int count = Integer.parseInt((String) Objects.requireNonNull(comboBox1.getSelectedItem()));
        try {
            String IP = new Room(port, count).getIPAddress();   //创建房间(服务器)，并且获取本机的局域网IP地址
            frame.setTitle(frame.getTitle()+" @"+IP+":"+port);  //将IP和端口号显示在房主客户端上
            player.connect("localhost", port);  //本机连接到房间，直接使用localhost就行了
            waitPlayers();  //进入等待玩家页面
        } catch (IOException e) {
            JOptionPane.showMessageDialog(rootPanel, "创建套接字失败","错误", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());    //设置为操作系统默认UI样式
        new Launcher();
        //new Launcher();   //如果不方便开两台电脑，就可以开两个客户端进行测试
    }
}
