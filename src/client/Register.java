package client;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class Register extends JFrame {

    private JPanel contentPane;
    private JTextField textField_UserName;
    private JPasswordField passwordField;
    private Socket socket;

    static String serverAddress;
    static Scanner in;
    static PrintWriter out;
    static boolean loginSuccess = false;
    static String userName = null;

    /**
     * Create the frame.
     */
    public Register(Socket s) {
        socket = s;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("ChatRoom");
        setBounds(100, 100, 351, 240);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);
        setLocationRelativeTo(null);// 居中显示Register窗口

        JLabel lblNewLabel = new JLabel("\u7528\u6237\u540D");
        lblNewLabel.setBounds(33, 60, 54, 24);
        contentPane.add(lblNewLabel);

        JLabel lblNewLabel_1 = new JLabel("\u5BC6\u7801");
        lblNewLabel_1.setBounds(33, 103, 54, 24);
        contentPane.add(lblNewLabel_1);

        textField_UserName = new JTextField();
        textField_UserName.setBounds(97, 57, 188, 27);
        contentPane.add(textField_UserName);
        textField_UserName.setColumns(10);

        passwordField = new JPasswordField();
        passwordField.setBounds(97, 100, 188, 27);
        contentPane.add(passwordField);

        JButton btn_Regist = new JButton("\u6CE8\u518C");
        btn_Regist.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        btn_Regist.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 点击注册按钮
                String pwd = String.valueOf(passwordField.getPassword());
                int length = pwd.length();

                if (length < 4) {
                    JOptionPane.showMessageDialog(null, "密码长度需要大于等于4");
                } else {
                    userName = textField_UserName.getText();
                    out.println("REGISTER " + textField_UserName.getText() + " " + pwd);
                    out.flush();
                }
            }
        });
        btn_Regist.setBounds(115, 148, 80, 27);
        contentPane.add(btn_Regist);

        JButton btn_Login = new JButton("\u767B\u5F55");
        btn_Login.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        btn_Login.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 点击登录按钮
                String pwd = String.valueOf(passwordField.getPassword());
                int length = pwd.length();

                if (length < 4) {
                    JOptionPane.showMessageDialog(null, "密码长度需要大于等于4");
                } else {
                    userName = textField_UserName.getText();
                    out.println("LOGIN " + textField_UserName.getText() + " " + pwd);
                    out.flush();
                }
            }
        });
        btn_Login.setBounds(205, 148, 80, 27);
        contentPane.add(btn_Login);

        JLabel lblNewLabel_TipMsg = new JLabel(
                "\u8BF7\u4F7F\u7528\u7528\u6237\u540D\u548C\u5BC6\u7801\u6CE8\u518C\u6216\u767B\u5F55");
        lblNewLabel_TipMsg.setBounds(33, 23, 252, 15);
        contentPane.add(lblNewLabel_TipMsg);
        // lblNewLabel_TipMsg.setText("请输入账号密码注册或登录");
    }

    public String run() {
        try {

            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String line = in.nextLine();

                if (line.startsWith("SUCCESSFULLYREGISTER")) {
                    // 注册成功
                    JOptionPane.showMessageDialog(null, "注册成功");
                    loginSuccess = true;
                    return textField_UserName.getText();
                } else if (line.startsWith("SUCCESSFULLYLOGIN")) {
                    // 登录成功
                    JOptionPane.showMessageDialog(null, "登录成功");
                    loginSuccess = true;
                    return textField_UserName.getText();
                } else if (line.startsWith("FAILEDTOLOGIN")) {
                    // 登录失败
                    JOptionPane.showMessageDialog(null, "登录失败，请检查用户名和密码");
                } else if (line.startsWith("FAILEDTOREGISTER")) {
                    // 注册失败
                    JOptionPane.showMessageDialog(null, "用户名已存在");
                } else {
                    // 收到无法识别字符串
                    System.out.println(line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
