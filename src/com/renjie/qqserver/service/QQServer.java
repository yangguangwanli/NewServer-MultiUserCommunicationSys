package com.renjie.qqserver.service;

import com.renjie.qqcommon.Message;
import com.renjie.qqcommon.MessageType;
import com.renjie.qqcommon.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Renjie
 * @version 1.0
 * 这是服务器, 在监听9999，等待客户端的连接，并保持通信
 */
public class QQServer {

    private ServerSocket ss = null;
    //创建一个集合，存放多个用户，如果是这些用户登录，就认为是合法
    //这里我们也可以使用 ConcurrentHashMap, 可以处理并发的集合，没有线程安全
    //HashMap 没有处理线程安全，因此在多线程情况下是不安全
    //ConcurrentHashMap 处理的线程安全,即线程同步处理, 在多线程情况下是安全
    private static ConcurrentHashMap<String, User> validUsers = new ConcurrentHashMap<>();

    static { //在静态代码块，初始化 validUsers

        validUsers.put("100", new User("100", "123456"));
        validUsers.put("200", new User("200", "123456"));
        validUsers.put("300", new User("300", "123456"));
        validUsers.put("周杰伦", new User("周杰伦", "123456"));
        validUsers.put("蔡依林", new User("蔡依林", "123456"));
        validUsers.put("林俊杰", new User("林俊杰", "123456"));
        validUsers.put("科比", new User("科比", "123456"));
        validUsers.put("乔丹", new User("乔丹", "123456"));

    }

    //验证用户是否有效的方法
    private boolean checkUser(String userId, String passwd) {

        User user = validUsers.get(userId);
        //一层一层验证
        if (user == null) {//说明userId没有存在validUsers 的key中
            return false;
        }
        if (!user.getPasswd().equals(passwd)) {//userId正确，但是密码错误
            return false;
        }

        return true;
    }

    public QQServer() {
        //注意：端口可以写在配置文件.
        try {
            System.out.println("服务端在9999端口监听...");
            //启动推送新闻/公共消息的线程
            new Thread(new SendNewsToAllService()).start();
            ss = new ServerSocket(9999);

            while (true) { //当和某个客户端连接后，会继续监听, 因此while
                Socket socket = ss.accept();//如果没有客户端连接，就会阻塞在这里
                //得到socket关联的对象输入流
                ObjectInputStream ois =
                        new ObjectInputStream(socket.getInputStream());
                //得到socket关联的对象输出流
                ObjectOutputStream oos =
                        new ObjectOutputStream(socket.getOutputStream());
                //读取客户端发送的User对象
                User u = (User) ois.readObject();
                //初始化一个Message对象，准备回复客户端
                Message message = new Message();

                //验证用户合法性
                if (checkUser(u.getUserId(), u.getPasswd())) {//通过检验
                    //已经有用户登录了
                    if (ManageClientThreads.getHm().containsKey(u.getUserId())) {
                        //这里回复后登录者
                        message.setMesType(MessageType.MESSAGE_ALREADY_LOGIN);
                        oos.writeObject(message);

                        //这里回复在线用户
                        Message message1 = new Message();
                        message1.setMesType(MessageType.MESSAGE_ALREADY_LOGIN);
                        message1.setSender("服务器");
                        message1.setGetter(u.getUserId());

                        ObjectOutputStream oos1 = new ObjectOutputStream(ManageClientThreads.
                                getServerConnectClientThread(u.getUserId()).getSocket().getOutputStream());
                        oos1.writeObject(message1);

                    } else {

                        //设置消息类型为成功登录
                        message.setMesType(MessageType.MESSAGE_LOGIN_SUCCEED);
                        //将message对象回复客户端
                        oos.writeObject(message);

                        //成功登录就创建一个线程，和客户端保持通信, 该线程需要持有socket对象
                        ServerConnectClientThread serverConnectClientThread =
                                new ServerConnectClientThread(socket, u.getUserId());
                        //启动该线程
                        serverConnectClientThread.start();
                        //把该线程对象，放入到一个集合中，进行管理.
                        ManageClientThreads.addClientThread(u.getUserId(), serverConnectClientThread);


                    }

                } else { // 登录失败
                    System.out.println("用户 id=" + u.getUserId() + " pwd=" + u.getPasswd() + " 验证失败");
                    //设置消息类型为失败登录，发给客户端就完事儿了
                    message.setMesType(MessageType.MESSAGE_LOGIN_FAIL);
                    oos.writeObject(message);
                    //关闭socket
                    socket.close();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            //如果服务器退出了while，说明服务器端不在监听，因此关闭ServerSocket
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
