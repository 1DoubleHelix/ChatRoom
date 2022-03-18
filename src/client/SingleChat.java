package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SingleChat extends JFrame {

    private JPanel contentPane;
    private JTextField textField_File;
    private JTextField textField_Msg;
    private Socket socket;
    private String friendName = null;
    public JTextArea textArea_Msg = null;

    private PrintWriter out;


    /**
     * Create the frame.
     */
    public SingleChat(Socket s) throws IOException {
        socket = s;
        out = new PrintWriter(socket.getOutputStream());
        setTitle("ChatRoom\u79C1\u804A");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);// 设置窗口关闭后状态
        setBounds(100, 100, 700, 450);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 47, 664, 259);
        contentPane.add(scrollPane);

        textArea_Msg = new JTextArea();
        scrollPane.setViewportView(textArea_Msg);

        JButton btn_SendMsg = new JButton("\u53D1\u9001\u6D88\u606F");
        btn_SendMsg.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 点击发送消息按钮
                out.println("PRIVATEMESSAGE " + friendName + " " + textField_Msg.getText());
                out.flush();
                textField_Msg.setText("");
            }
        });
        btn_SendMsg.setBounds(581, 327, 93, 27);
        contentPane.add(btn_SendMsg);

        JButton btn_ViewFile = new JButton("\u6D4F\u89C8");
        btn_ViewFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 点击浏览按钮
                JFileChooser choice = new JFileChooser();
                choice.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);  //浏览文件夹
                int ret = choice.showOpenDialog(SingleChat.super.rootPane);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    textField_File.setText(choice.getSelectedFile().getPath());
                }
            }
        });
        btn_ViewFile.setBounds(478, 364, 93, 27);
        contentPane.add(btn_ViewFile);

        textField_File = new JTextField();
        textField_File.setBounds(94, 364, 374, 27);
        contentPane.add(textField_File);
        textField_File.setColumns(10);

        JButton btn_SendFile = new JButton("\u53D1\u9001\u6587\u4EF6");
        btn_SendFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 点击发送文件按钮
                ClientFileThread.outFileToServer(textField_File.getText(), friendName);  //发送文件给服务器
            }
        });
        btn_SendFile.setBounds(581, 364, 93, 27);
        contentPane.add(btn_SendFile);

        textField_Msg = new JTextField();
        textField_Msg.setBounds(94, 327, 477, 27);
        contentPane.add(textField_Msg);
        textField_Msg.setColumns(10);

        JLabel lblNewLabel = new JLabel("\u6587\u4EF6\u8DEF\u5F84");
        lblNewLabel.setBounds(30, 370, 54, 15);
        contentPane.add(lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel("\u8F93\u5165\u6D88\u606F");
        lblNewLabel_1.setBounds(30, 333, 54, 15);
        contentPane.add(lblNewLabel_1);
    }

    public void setName(String fn) {
        friendName = fn;
        JLabel lbl_FriendName = new JLabel("正在和" + friendName + "聊天");
        // Lable显示聊天对象
        // 上级窗口传入聊天对象时在此处理
        // lblNewLabel_FriendName.setText("UserName");
        lbl_FriendName.setBounds(10, 10, 197, 27);
        contentPane.add(lbl_FriendName);
    }

}
