package client;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.JList;
import javax.swing.JScrollPane;

public class MainFrame extends JFrame {

    private JPanel contentPane;
    private JTextField textField_AddFriend;

    static String serverAddress;
    static Scanner in;
    static PrintWriter out;
    static String[] friendList;
    static String myName;
    static Socket socket = null;
    static DefaultListModel L = new DefaultListModel();
    static Map<String, SingleChat> singleChatMap = new HashMap<>(); // 保存与各个用户私聊窗口
    private static JTextField textField_GroupID;
    static GroupChat groupChat = null;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {

        // 建立连接
        serverAddress = "172.18.19.64";// 服务器IP地址

        try {
            socket = new Socket(serverAddress, 59001);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream());

        } catch (IOException e1) {
            // TODO 自动生成的 catch 块
            e1.printStackTrace();
        }

        try {
            // 显示注册登录窗口
            Register frameRigiste = new Register(socket);
            frameRigiste.setVisible(true);
            myName = frameRigiste.run();
            // 登录成功
            while (Register.loginSuccess == false) {
            }
            frameRigiste.setVisible(false);// 关闭注册登录窗口

            // 打开ChatRom主窗口
            MainFrame frameClient = new MainFrame();
            frameClient.setTitle("当前用户：" + myName);
            frameClient.setVisible(true);

            ClientFileThread fileThread = new ClientFileThread(myName, frameClient, out);
            fileThread.start(); // 开启发送文件线程

            // 服务器返回信息处理
            while (in.hasNextLine()) {
                String line = in.nextLine();

                if (line.startsWith("FRIENDALREADYEXISTS")) {
                    // 好友已存在
                    JOptionPane.showMessageDialog(null, "好友已存在");
                } else if (line.startsWith("USERNOTFOUND")) {
                    // 用户名不存在
                    JOptionPane.showMessageDialog(null, "用户名不存在");
                } else if (line.startsWith("SUCCESSFULLYADDTHEFRIEND")) {
                    // 添加成功
                    JOptionPane.showMessageDialog(null, "好友添加成功");
                } else if (line.startsWith("FRIENDSLIST")) {
                    // 收到好友列表
                    friendList = line.split("\\s");
                    L.clear();// 清除列表
                    for (int i = 1; i < (friendList.length); i++) {
                        L.addElement(friendList[i]);// 重新显示
                    }
                } else if (line.startsWith("PRIVATEMESSAGE")) {
                    // 收到私聊消息
                    recievePrivateMessage(line);
                } else if (line.startsWith("FRIENDNOTONLINE")) {
                    JOptionPane.showMessageDialog(null, "好友不在线");
                } else if (line.startsWith("CREATEGROUPSUCCESSFULLY")) {
                    // 创建群聊成功
                    // 创建群聊窗口
                    groupChat = new GroupChat(socket);
                    groupChat.setID(textField_GroupID.getText());
                    groupChat.setTitle("群聊：" + textField_GroupID.getText());
                    groupChat.setVisible(true);
                } else if (line.startsWith("GROUPMESSAGE")) {
                    // 收到群聊消息
                    recieveGroupMessage(line);

                } else if (line.startsWith("GROUPALREADYEXISTS")) {
                    // 创建群聊失败
                    JOptionPane.showMessageDialog(null, "创建群聊失败");
                } else if (line.startsWith("JOINGROUPSUCCESSFULLY")) {
                    // 加入群聊成功
                    // 创建群聊窗口
                    groupChat = new GroupChat(socket);
                    groupChat.setID(textField_GroupID.getText());
                    groupChat.setTitle("群聊：" + textField_GroupID.getText());
                    groupChat.setVisible(true);
                } else if (line.startsWith("GROUPNOTEXISTS")) {
                    // 群聊不存在
                    JOptionPane.showMessageDialog(null, "群聊不存在");
                } else {
                    // 收到无法识别字符串
                    System.out.println(line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the frame.
     */
    public MainFrame() {
        setTitle("ChatClient\u5BA2\u6237\u7AEF");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 700, 430);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        contentPane.add(tabbedPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        tabbedPane.addTab("\u6DFB\u52A0\u597D\u53CB", null, panel, null);
        panel.setLayout(null);

        JLabel lblNewLabel = new JLabel("\u8BF7\u8F93\u5165\u7528\u6237\u540D\u6DFB\u52A0\u597D\u53CB");
        lblNewLabel.setBounds(10, 10, 143, 17);
        panel.add(lblNewLabel);

        textField_AddFriend = new JTextField();
        textField_AddFriend.setBounds(10, 37, 546, 27);
        panel.add(textField_AddFriend);
        textField_AddFriend.setColumns(10);

        JButton btn_Add = new JButton("\u6DFB\u52A0");
        btn_Add.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 点击添加（好友）按钮
                out.println("REQUESTFRIEND " + textField_AddFriend.getText());
                out.flush();
            }
        });
        btn_Add.setBounds(566, 37, 93, 27);
        panel.add(btn_Add);

        JPanel panel_1 = new JPanel();

        tabbedPane.addTab("\u597D\u53CB", null, panel_1, null);
        panel_1.setLayout(null);

        JLabel lblNewLabel_1 = new JLabel("\u7528\u6237\u5217\u8868");
        lblNewLabel_1.setBounds(10, 10, 71, 17);
        panel_1.add(lblNewLabel_1);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 37, 649, 227);
        panel_1.add(scrollPane);

        JList list_Friend = new JList(L);
        scrollPane.setViewportView(list_Friend);

        list_Friend.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // 点击列表中的好友
                try {
                    String friendName = list_Friend.getSelectedValue().toString();
                    System.out.println(friendName);
                    SingleChat singleChat = null;
                    if (!singleChatMap.containsKey(friendName)) {
                        try {
                            singleChat = new SingleChat(socket); // 为该好友创建窗口
                            singleChatMap.put(friendName, singleChat);
                            singleChat.setName(friendName);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } else
                        singleChat = singleChatMap.get(friendName);
                    singleChat.setVisible(true);
                } catch (Exception ee) {

                }

            }
        });

        JButton btn_GetFriendList = new JButton("\u83B7\u53D6\u597D\u53CB\u5217\u8868");
        btn_GetFriendList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                out.println("SHOWFRIENDS");
                out.flush();
            }
        });
        btn_GetFriendList.setBounds(537, 274, 122, 27);
        panel_1.add(btn_GetFriendList);

        JLabel lblNewLabel_2 = new JLabel("\u70B9\u51FB\u7528\u6237\u540D\u5F00\u59CB\u79C1\u804A");
        lblNewLabel_2.setBounds(20, 274, 131, 17);
        panel_1.add(lblNewLabel_2);

        JPanel panel_2 = new JPanel();
        tabbedPane.addTab("\u7FA4\u804A", null, panel_2, null);
        panel_2.setLayout(null);

        JLabel lblNewLabel_3 = new JLabel("\u8F93\u5165\u7FA4\u804AID");
        lblNewLabel_3.setBounds(10, 10, 110, 17);
        panel_2.add(lblNewLabel_3);

        textField_GroupID = new JTextField();
        textField_GroupID.setBounds(10, 37, 443, 27);
        panel_2.add(textField_GroupID);
        textField_GroupID.setColumns(10);

        JButton btn_CreatGroup = new JButton("\u521B\u5EFA\u7FA4\u804A");
        btn_CreatGroup.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 点击创建群聊按钮
                out.println("CREATEGROUP " + textField_GroupID.getText());
                out.flush();
            }
        });
        btn_CreatGroup.setBounds(463, 37, 93, 27);
        panel_2.add(btn_CreatGroup);

        JButton btn_JoinGroup = new JButton("\u52A0\u5165\u7FA4\u804A");
        btn_JoinGroup.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 点击加入群聊按钮
                out.println("JOINGROUP " + textField_GroupID.getText());
                out.flush();
            }
        });
        btn_JoinGroup.setBounds(566, 37, 93, 27);
        panel_2.add(btn_JoinGroup);
    }

    public static void recievePrivateMessage(String line) throws IOException {
        // 接收私聊消息
        // 显示消息
        String[] msg = line.split("\\s", 4);
        String sendName = msg[1]; // 发送方
        String receiveName = msg[2]; // 接收方
        String message = msg[3];
        SingleChat singleChat;

        if (sendName.equals(myName)) // 收到的消息是自己的
        {
            if (!singleChatMap.containsKey(receiveName)) {
                singleChat = new SingleChat(socket); // 创建与该好友聊天窗口
                singleChatMap.put(sendName, singleChat);
                singleChat.setName(sendName);
            }
            singleChat = singleChatMap.get(receiveName);
            singleChat.textArea_Msg.append(sendName + "：" + message + "\n");
            return;
        }

        if (!singleChatMap.containsKey(sendName)) { // 收到的消息是好友的，且当前未建立与其聊天窗口
            singleChat = new SingleChat(socket); // 创建与该好友聊天窗口
            singleChatMap.put(sendName, singleChat);
            singleChat.setName(sendName);
        }
        singleChat = singleChatMap.get(sendName);
        singleChat.setVisible(true);
        singleChat.textArea_Msg.append(sendName + "：" + message + "\n");
    }

    private static void recieveGroupMessage(String line) throws IOException {
        // 接收群聊消息
        String[] msg = line.split("\\s", 4);
        String sendName = msg[2];// 发送方
        String message = msg[3];// 接收消息
        groupChat.textArea_GroupMsg.append(sendName + "：" + message + "\n");
    }
}
