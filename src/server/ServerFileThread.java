package server;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;


public class ServerFileThread extends Thread {
    ServerSocket server = null;
    Socket socket = null;
    public static Map<String, Socket> allSockets = new HashMap<>();

    public void run() {
        try {
            server = new ServerSocket(8090);
            while (true) {
                socket = server.accept();
//                list.add(socket);
                // 开启文件传输线程
                FileReadAndWrite fileReadAndWrite = new FileReadAndWrite(socket);
                fileReadAndWrite.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class FileReadAndWrite extends Thread {
    private Socket nowSocket = null;
    private DataInputStream input = null;
    private DataOutputStream output = null;
    String userName = null;

    public FileReadAndWrite(Socket socket) {
        this.nowSocket = socket;
    }

    public void run() {
        try {
            input = new DataInputStream(nowSocket.getInputStream());  // 输入流
            userName = input.readUTF();  //接入客户端用户名
            ServerFileThread.allSockets.put(userName, nowSocket);  //将当前用户和其对应socket放入map中
            while (true) {
                // 获取文件名字和文件长度
                String textName = input.readUTF();
                long textLength = input.readLong();
                String receiveName = input.readUTF();//接收方名字
                // 发送文件名字和文件长度给所有客户端
                Socket receiveSocket = ServerFileThread.allSockets.get(receiveName);
                output = new DataOutputStream(receiveSocket.getOutputStream());  // 输出流
                output.writeUTF(textName);
                output.flush();
                output.writeLong(textLength);
                output.flush();

                // 发送文件内容
                int length = -1;
                long curLength = 0;
                byte[] buff = new byte[1024];
                while ((length = input.read(buff)) > 0) {
                    curLength += length;
                    output = new DataOutputStream(receiveSocket.getOutputStream());  // 输出流
                    output.write(buff, 0, length);
                    output.flush();
                    if (curLength == textLength) {  // 强制退出
                        break;
                    }
                }
            }
        } catch (Exception e) {
            ServerFileThread.allSockets.remove(userName);  // 线程关闭，移除相应套接字
        }
    }
    /*public void run() {
        try {
            input = new DataInputStream(nowSocket.getInputStream());  // 输入流
            while (true) {
                // 获取文件名字和文件长度
                String textName = input.readUTF();
                long textLength = input.readLong();
                // 发送文件名字和文件长度给所有客户端
                for(Socket socket: server.ServerFileThread.list) {
                    output = new DataOutputStream(socket.getOutputStream());  // 输出流
                    if(socket != nowSocket) {  // 发送给其它客户端
                        output.writeUTF(textName);
                        output.flush();
                        output.writeLong(textLength);
                        output.flush();
                    }
                }
                // 发送文件内容
                int length = -1;
                long curLength = 0;
                byte[] buff = new byte[1024];
                while ((length = input.read(buff)) > 0) {
                    curLength += length;
                    for(Socket socket: server.ServerFileThread.list) {
                        output = new DataOutputStream(socket.getOutputStream());  // 输出流
                        if(socket != nowSocket) {  // 发送给其它客户端
                            output.write(buff, 0, length);
                            output.flush();
                        }
                    }
                    if(curLength == textLength) {  // 强制退出
                        break;
                    }
                }
            }
        } catch (Exception e) {
            server.ServerFileThread.list.remove(nowSocket);  // 线程关闭，移除相应套接字
        }
    }*/
}
