package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static Map<String, String> users = new HashMap<>();  //保存所有用户名和密码
    private static Map<String, Set> relationship = new HashMap<>();  //存储所有用户好友
    private static Groups groups = new Groups();  //所有群聊
    private static Map<String, PrintWriter> allWriters = new HashMap<>();

    static class Group {
        private Map<String, PrintWriter> writers = new HashMap<>();  //接收本群所有writer，以在该群广播
        public int count = 0;  //当前群聊人数
        public String ID;

        public Group(String ID) {
            this.ID = ID;
        }

        public void addMember(String name, PrintWriter writer) {
            synchronized (writers) {
                writers.put(name, writer);
            }
            count++;

        }

        public void deleteMember(String name) {
            synchronized (writers) {
                writers.remove(name);
            }
            count--;
            groupChat("SYSTEM", name + " is leaving");  //群内广播该用户退出群聊

        }

        public void groupChat(String name, String para) {  //para即为message
            for (PrintWriter writer : writers.values()) {
                writer.println("GROUPMESSAGE " + "ID " + name + " " + para);
            }
        }
    }

    static class Groups {
        Map<String, Group> groups = new HashMap<>();

        public void createGroup(String para, String name, PrintWriter out) {
            if (groups.containsKey(para)) {
                out.println("GROUPALREADYEXISTS");
                return;
            } else {
                Group group = new Group(para);
                group.addMember(name, out);
                groups.put(para, group);
                out.println("CREATEGROUPSUCCESSFULLY");
                group.groupChat("SYSTEM", name + " has joined");  //群内广播该用户加入群聊
            }
        }

        public void joinGroup(String para, String name, PrintWriter out) {
            if (groups.containsKey(para)) {
                Group group = groups.get(para);
                group.addMember(name, out);
                out.println("JOINGROUPSUCCESSFULLY");
                group.groupChat("SYSTEM", name + " has joined");  //群内广播该用户加入群聊
                return;
            } else {
                out.println("GROUPNOTEXISTS");
            }
        }

        public void groupChat(String para, String name) {
            String[] strs = para.split("\\s", 2);
            String ID = strs[0];
            String message = strs[1];
            groups.get(ID).groupChat(name, message);
        }

        public void deleteMember(String para, String name) {
            String ID = para;
            Group group = groups.get(ID);
            group.deleteMember(name);
            if (group.count == 0)
                groups.remove(ID);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        ServerFileThread serverFileThread = new ServerFileThread();  //开启服务端文件传输进程
        serverFileThread.start();
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(59001)) {
            while (true) {
                pool.execute(new Handler(listener.accept(), groups));  //开启服务端传输普通信息/命令进程
            }
        }
    }

    /**
     * The client handler task.
     */
    private static class Handler implements Runnable {
        private String name = null;  //用户名
        private Socket socket;
        private Scanner in;
        private PrintWriter out;
        private Set<String> friends;
        private Groups groups;

        /**
         * Constructs a handler thread, squirreling away the socket. All the interesting
         * work is done in the run method. Remember the constructor is called from the
         * server's main method, so this has to be as short as possible.
         */
        public Handler(Socket socket, Groups groups) {  //构造函数
            try {
                this.socket = socket;
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);
                friends = new HashSet<>();
                this.groups = groups;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean createUser(String name, String password) {
            synchronized (users) {
                if (users.containsKey(name)) {  //存在用户
                    return false;
                } else {
                    users.put(name, password);
                    return true;
                }
            }
        }

        public boolean allowLogin(String name, String password) {
            synchronized (users) {
                if (users.containsKey(name)) {  //存在用户
                    if ((users.get(name)).equals(password))  //密码正确
                        return true;
                    else return false;  //密码不正确
                } else {
                    return false;  //不存在用户
                }
            }
        }

        public void loginOrRegister() {
            while (true) {
//                out.println("LOGINORREGISTER");  //要求客户端登录或者注册
                String str = in.nextLine();
                String[] strs = str.split("\\s");
                String command = strs[0];
                String name = strs[1];
                String password = strs[2];

                if (command.equals("REGISTER")) {
                    boolean flag = createUser(name, password);
                    if (flag) {
                        this.name = name;
                        synchronized (allWriters) {
                            allWriters.put(name, out);    //表明该用户在线
                        }
                        synchronized (relationship) {
                            relationship.put(name, friends);  //放入空的friends集合
                        }
                        out.println("SUCCESSFULLYREGISTER");
                        return;  //注册成功接着往后面执行
                    } else {
                        out.println("FAILEDTOREGISTER");
                        continue;
                    }

                } else {  //用户请求登录
                    boolean flag = allowLogin(name, password);
                    if (flag) {
                        this.name = name;
                        synchronized (allWriters) {
                            allWriters.put(name, out);    //表明该用户在线
                        }
                        out.println("SUCCESSFULLYLOGIN");

                        return;  //登录成功接着往后面执行
                    } else {
                        out.println("FAILEDTOLOGIN");
                        continue;
                    }
                }
            }
        }

        public void loadFriend() {
            synchronized (relationship) {
                this.friends = relationship.get(this.name);  //将当前用户朋友传给其friends变量
            }
        }

        public void requestFriend(String para) {  //此函数参数为用户名
            if (!users.containsKey(para))  //不存在该用户
            {
                out.println("USERNOTFOUND");
                return;
            } else if (friends.contains(para)) {  //该用户已经是好友
                out.println("FRIENDALREADYEXISTS");
                return;
            } else {
                friends.add(para);  //本用户添加好友
                synchronized (relationship) {
                    relationship.get(para).add(this.name);  //对面也添加自己
                }
                out.println("SUCCESSFULLYADDTHEFRIEND");
                return;
            }

        }

        public void listFriend() {
            String friendStr = "";
            if (friends.isEmpty()) {
                out.println("FRIENDSLIST ");
                return;
            }
            for (String friend : friends) {
                friendStr = friendStr + friend + " ";
            }
            friendStr = friendStr.substring(0, friendStr.length() - 1);  //删除最后一个空格
            friendStr = "FRIENDSLIST " + friendStr;
            out.println(friendStr);
        }

        public void privateChat(String para) {
            PrintWriter friendWriter = null;
            String[] strs = para.split("\\s", 2);
            String friendname = strs[0];
            String message = strs[1];
            try {
                if (!allWriters.containsKey(friendname)) {  //用户不在线
                    out.println("FRIENDNOTONLINE");
                    return;
                }
                friendWriter = allWriters.get(friendname);
                friendWriter.println("PRIVATEMESSAGE " + name + " " + friendname + " " + message);  //将该消息传递给对面客户端
                out.println("PRIVATEMESSAGE " + name + " " + friendname + " " + message);  //将该消息传递给本客户端
//                while (true){
//                    if(!allWriters.containsKey(para)){  //用户中途下线
//                        out.println("FRIENDNOTONLINE");
//                        return;
//                    }
//                }
            } catch (Exception e) {

            }
//            finally {
//                if(friendWriter!=null)
//                    friendWriter.close();
//            }

        }

        public void run() {
            try {
                loginOrRegister();
                loadFriend();
                while (true) {
                    String input = in.nextLine();  //持续接收来自客户端输入
                    System.out.println(input);
                    String[] strs = input.split("\\s", 2);
                    String command = strs[0];
                    String para = null;
                    if (strs.length > 1)
                        para = strs[1];
                    else para = null;
                    switch (command) {  //判断客户端传来的命令类型
                        case "REQUESTFRIEND":  //完整的格式为：REQUESTFRIEND friendname
                            requestFriend(para);
                            break;
                        case "PRIVATEMESSAGE":  //完整的格式为：PRIVATEMESSAGE friendname message
                            privateChat(para);
                            break;
                        case "SHOWFRIENDS":  //完整的格式为：SHOWFRIENDS
                            listFriend();
                            break;
                        case "CREATEGROUP":  //完整的格式为：CREATEGROUP ID
                            groups.createGroup(para, this.name, this.out);
                            break;
                        case "JOINGROUP":  //完整的格式为：JOINGROUP ID
                            groups.joinGroup(para, this.name, out);
                            break;
                        case "GROUPMESSAGE":  //完整的格式为：GROUPMESSAGE ID MESSAGE
                            groups.groupChat(para, this.name);
                            break;
                        case "LEAVEGROUP":  //完整的格式为：LEAVEGROUP ID
                            groups.deleteMember(para, this.name);
                            break;
                        default:
                            System.out.println(input);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {  //退出前先删掉该用户对应writer
                if (out != null) {
                    allWriters.remove(name);
                    out.println("ERROR!!!");
                    out.close();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}