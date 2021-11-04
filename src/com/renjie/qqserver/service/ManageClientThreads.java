package com.renjie.qqserver.service;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Renjie
 * @version 1.0
 * 该类为线程管理器，用于管理和客户端通信的线程，ConcurrentHashMap是线程安全的集合，保存UserId和线程数据
 */
public class ManageClientThreads {
    //hashmap 进行存储  userId - 线程  一一对应
    private static ConcurrentHashMap<String, ServerConnectClientThread> hm = new ConcurrentHashMap<>();

    //返回 hm
    public static ConcurrentHashMap<String, ServerConnectClientThread> getHm() {
        return hm;
    }

    //添加线程对象到 hm 集合
    public static void addClientThread(String userId, ServerConnectClientThread serverConnectClientThread) {

        hm.put(userId, serverConnectClientThread);

    }

    //根据userId 返回ServerConnectClientThread线程
    public static ServerConnectClientThread getServerConnectClientThread(String userId) {
        return hm.get(userId);
    }

    //增加一个方法，从集合中，移除某个线程对象
    public static void removeServerConnectClientThread(String userId) {
        hm.remove(userId);
    }

    //这里编写方法，可以返回在线用户列表
    public static String getOnlineUser() {
        //集合遍历 ，遍历 hashmap的key
        Iterator<String> iterator = hm.keySet().iterator();
        String onlineUserList = "";
        //以字符串的形式返回，中间空格隔开
        while (iterator.hasNext()) {
            onlineUserList += iterator.next().toString() + " ";
        }
        return onlineUserList;
    }
}
