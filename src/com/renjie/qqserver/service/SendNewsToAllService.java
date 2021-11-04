package com.renjie.qqserver.service;


import com.renjie.qqcommon.Message;
import com.renjie.qqcommon.MessageType;
import com.renjie.utils.Utility;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Renjie
 * @version 1.0
 * <p>
 * //推送新闻的方式与群聊一样，循环遍历每个在线用户发送消息即可
 */
public class SendNewsToAllService implements Runnable {


    @Override
    public void run() {

        //为了可以推送多次新闻，使用while
        while (true) {
            System.out.println("请输入服务器要推送的新闻/消息[输入exit表示退出推送服务线程]");
            //这里直接输入服务器要推送的消息
            String news = Utility.readString(100);
            if ("exit".equals(news)) {
                break;
            }
            //构建一个消息 , 群发消息
            Message message = new Message();
            message.setSender("服务器");
            message.setMesType(MessageType.MESSAGE_TO_ALL_MES);
            message.setContent(news);
            //写入当前时间
            message.setSendTime(new Date().toString());
            System.out.println("服务器推送消息给所有人 说: " + news);


            //2021.11.04出现空指针异常，试图完成服务器发消息保存离线信息，导致空指针异常

            //遍历当前所有的通信线程，得到socket,并发送message


            ConcurrentHashMap.KeySetView<String, ArrayList<Message>> keyset =
                    ManageOfflineMessages.getOffLineDb().keySet();

            ConcurrentHashMap<String, ServerConnectClientThread> hm = ManageClientThreads.getHm();
            for (String user : keyset) {
                //在线的用户之间发送
                if (hm.containsKey(user)) {
//                    Iterator<String> iterator = hm.keySet().iterator();
//                    while (iterator.hasNext()) {
//                        String onLineUserId = iterator.next();
                    try {
                        ObjectOutputStream oos =
                                new ObjectOutputStream(hm.get(user).getSocket().getOutputStream());
                        oos.writeObject(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //在线的用户之间发送
                    //不属于在线列表的话 //过滤那些在线用户，对于不在线的用户也保留离线信息
//                } else {
//                    message.setMesType(MessageType.MESSAGE_OFFLINE_MES);
//                    ManageOfflineMessages.addOfflineMessages(user, message);
//                }
                }
            }
        }
    }
}

