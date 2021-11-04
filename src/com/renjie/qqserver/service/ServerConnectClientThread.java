package com.renjie.qqserver.service;

import com.renjie.qqcommon.Message;
import com.renjie.qqcommon.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Renjie
 * @version 1.0
 * 该类的一个对象和某个客户端保持通信，线程持续循环
 */
public class ServerConnectClientThread extends Thread {

    private Socket socket;
    private String userId;//连接到服务端的用户id


    public ServerConnectClientThread(Socket socket, String userId) {
        this.socket = socket;
        this.userId = userId;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() { //这里线程处于run的状态，可以发送/接收消息

        while (true) {
            try {
                //2021.11.04
                if (ManageOfflineMessages.checkMessages(userId)) {
                    ConcurrentHashMap<String, ArrayList<Message>> offLineDb = ManageOfflineMessages.getOffLineDb();

                    ArrayList<Message> messages = offLineDb.get(userId);
                    while (!messages.isEmpty()) {
                        for (Message message : messages) {
                            ServerConnectClientThread serverConnectClientThread =
                                    ManageClientThreads.getServerConnectClientThread(message.getGetter());

                            ObjectOutputStream oos =
                                    new ObjectOutputStream(serverConnectClientThread.getSocket().getOutputStream());
                            oos.writeObject(message);
                            ManageOfflineMessages.removeOfflineMessages(userId, message);
                        }
                    }
                }


                System.out.println("服务端和客户端" + userId + " 保持通信，读取数据..." + new Date().getTime());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) ois.readObject();
                //线程循环里，根据message的类型，做相应的业务处理
                //发送在线用户列表，消息类型为 请求在线列表
                if (message.getMesType().equals(MessageType.MESSAGE_GET_ONLINE_FRIEND)) {
                    //客户端要在线用户列表
                    /*
                    在线用户列表形式设计
                     */
                    System.out.println(message.getSender() + " 请求在线用户列表");
                    String onlineUser = ManageClientThreads.getOnlineUser();
                    //返回message
                    //构建一个Message 对象，返回给客户端
                    Message message1 = new Message();
                    //返还的消息类型为 回复在线列表
                    message1.setMesType(MessageType.MESSAGE_RET_ONLINE_FRIEND);
                    message1.setContent(onlineUser);
                    message1.setSendTime(new Date().toString());//发送时间设置到message对象
                    message1.setGetter(message.getSender());
                    //返回给客户端
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(message1);

                    //普通消息
                } else if (message.getMesType().equals(MessageType.MESSAGE_COMM_MES)) {
                    //根据message获取getter id, 然后在得到对应先线程
                    //对方在线，直接发
                    if (ManageClientThreads.getServerConnectClientThread(message.getGetter()) != null) {
                        ServerConnectClientThread serverConnectClientThread =
                                ManageClientThreads.getServerConnectClientThread(message.getGetter());
                        //得到对应socket的对象输出流，将message对象转发给指定的客户端
                        ObjectOutputStream oos =
                                new ObjectOutputStream(serverConnectClientThread.getSocket().getOutputStream());
                        oos.writeObject(message);//转发，提示如果客户不在线，可以保存到数据库，这样就可以实现离线留言

                    }//如果接收信息的用户不在线，就把普通消息放到离线存储器里面
                    else {
                        message.setMesType(MessageType.MESSAGE_OFFLINE_MES);
                        ManageOfflineMessages.addOfflineMessages(message.getGetter(), message);

                    }
                    //转发，提示如果客户不在线，可以保存到数据库，这样就可以实现离线留言

                    //群聊消息
                } else if (message.getMesType().equals(MessageType.MESSAGE_TO_ALL_MES)) {
                    //需要遍历 管理线程的集合，把所有的线程的socket得到，然后把message进行转发即可
                    ConcurrentHashMap<String, ServerConnectClientThread> hm = ManageClientThreads.getHm();

                    Iterator<String> iterator = hm.keySet().iterator();
                    while (iterator.hasNext()) {

                        //取出在线用户id
                        String onLineUserId = iterator.next().toString();

                        if (!onLineUserId.equals(message.getSender())) {//排除群发消息的这个用户
                            //进行转发message
                            ObjectOutputStream oos =
                                    new ObjectOutputStream(hm.get(onLineUserId).getSocket().getOutputStream());
                            oos.writeObject(message);
                        }

                    }

                    //文件信息
                } else if (message.getMesType().equals(MessageType.MESSAGE_FILE_MES)) {
                    //对方在线
                    if (ManageClientThreads.getServerConnectClientThread(message.getGetter()) != null) {
                        //根据getter id 获取到对应的线程，将message对象转发
                        ObjectOutputStream oos =
                                new ObjectOutputStream(ManageClientThreads.getServerConnectClientThread(message.getGetter()).getSocket().getOutputStream());
                        //转发
                        oos.writeObject(message);

                    } else {//如果接收文件的用户不在线，就把文件放到离线存储器里面

                        message.setMesType(MessageType.MESSAGE_OFFLINE_FILE);
                        ManageOfflineMessages.addOfflineMessages(message.getGetter(), message);

                    }

                    //客户端退出消息
                } else if (message.getMesType().equals(MessageType.MESSAGE_CLIENT_EXIT)) {
                    System.out.println(message.getSender() + " 退出");
                    //将这个客户端对应线程，从集合删除.
                    ManageClientThreads.removeServerConnectClientThread(message.getSender());
                    socket.close();//关闭连接
                    //退出线程
                    break;

                    //重复登录信息直接转发即可
                } else if (message.getMesType().equals(MessageType.MESSAGE_ALREADY_LOGIN)) {
                    //根据getter id 获取到对应的线程，将message对象转发
                    ObjectOutputStream oos =
                            new ObjectOutputStream(ManageClientThreads.getServerConnectClientThread(message.getGetter()).getSocket().getOutputStream());
                    //转发
                    oos.writeObject(message);

                } else {
                    System.out.println("其他类型的message , 暂时不处理");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
