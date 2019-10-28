import com.swh.client.Player;
import com.swh.server.Room;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

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

    public Launcher() {
        imageLabel.setIcon(new ImageIcon("assets/cardpicture.png"));//在打包jar时用下面一条以访问包里面的资源
        //imageLabel.setIcon(new ImageIcon(getClass().getResource("assets/cardpicture.png")));//在打包为jar时也能获取包里面的资源
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
            if (name == null)
                System.exit(1);
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(frame.getContentPane(), "名称不能为空！", "提醒", JOptionPane.WARNING_MESSAGE);
            }else break;
        }
        frame.setTitle(name);
        player = new Player(name);

        enterButton.addActionListener(e -> new Thread(this::enterRoom).start());
    }

    private void waitPlayers() {
        ((CardLayout) rootPanel.getLayout()).show(rootPanel, "Card4");
        while (true) {
            try {
                String rec = player.read();
                if (rec.equals("begin")) {
                    frame.setVisible(false);    //游戏开始，隐藏Launcher
                    runGame();
                    return;
                } else {
                    rec = rec.replaceAll(" ", "\n");
                    textArea1.removeAll();
                    textArea1.append(rec);
                }
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("消息接收时出现错误");
            }
        }

    }

    private void runGame() {
        System.out.println("游戏开始");
        gameFrame gameFrame = new gameFrame(player);
        gameFrame.Ready();
    }

    private void enterRoom() {
        String ipAddress = IPTextField.getText();
        int port = Integer.parseInt(portTextField.getText());
        try {
            player.connect(ipAddress, port);
            waitPlayers();
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
            String IP = new Room(port, count).getIPAddress();
            frame.setTitle(frame.getTitle()+" @"+IP+":"+port);
            player.connect("localhost", port);
            waitPlayers();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(rootPanel, "创建套接字失败","错误", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        System.out.println(System.getProperty("user.dir"));
        new Launcher();
        //new Launcher();
    }
}
