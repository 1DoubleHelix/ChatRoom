package client;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class GroupChat extends JFrame {

    private JPanel contentPane;
    private JTextField textField_GroupMsg;
    private Socket socket;
    private PrintWriter out;
    public JTextArea textArea_GroupMsg;
    static String groupID = null;

    /**
     * Create the frame.
     *
     * @throws IOException
     */
    public GroupChat(Socket s) throws IOException {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 关闭群聊窗口
                out.println("LEAVEGROUP " + groupID);
                out.flush();
                e.getWindow().dispose();
            }
        });

        socket = s;
        out = new PrintWriter(socket.getOutputStream());

        setTitle("\u7FA4\u804A");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 600, 380);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        textField_GroupMsg = new JTextField();
        textField_GroupMsg.setBounds(10, 285, 461, 27);
        contentPane.add(textField_GroupMsg);
        textField_GroupMsg.setColumns(10);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 10, 564, 265);
        contentPane.add(scrollPane);

        textArea_GroupMsg = new JTextArea();
        scrollPane.setViewportView(textArea_GroupMsg);

        JButton btn_SendGroupMsg = new JButton("\u53D1\u9001\u6D88\u606F");
        btn_SendGroupMsg.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 点击发送消息
                out.println("GROUPMESSAGE " + groupID + " " + textField_GroupMsg.getText());
                out.flush();
                textField_GroupMsg.setText("");
            }
        });
        btn_SendGroupMsg.setBounds(481, 285, 93, 27);
        contentPane.add(btn_SendGroupMsg);
    }

    public void setID(String gName) {
        groupID = gName;
        setTitle(groupID);
    }
}
