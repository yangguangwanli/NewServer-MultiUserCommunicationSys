package com.renjie.qqserver.service;

import com.renjie.qqcommon.Message;

import java.util.ArrayList;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Auther: Renjie
 * @Date: 2021/11/3 - 20:53
 * @Description: 此公共类管理离线消息
 * @Version: 1.0
 */
public class ManageOfflineMessages {

    public static ConcurrentHashMap<String, ArrayList<Message>> offLineDb = new ConcurrentHashMap<>();

    static { //在静态代码块，初始化

        offLineDb.put("100", new ArrayList<Message>());
        offLineDb.put("200", new ArrayList<Message>());
        offLineDb.put("300", new ArrayList<Message>());
        offLineDb.put("周杰伦", new ArrayList<Message>());
        offLineDb.put("蔡依林", new ArrayList<Message>());
        offLineDb.put("林俊杰", new ArrayList<Message>());
        offLineDb.put("科比", new ArrayList<Message>());
        offLineDb.put("乔丹", new ArrayList<Message>());

    }

    //验证某个用户是否有离线消息
    public static boolean checkMessages(String userId) {
        ArrayList<Message> messages = offLineDb.get(userId);
        if (messages != null) {
            return true;
        } else {
            return false;
        }
    }

    public static ArrayList<Message> getOfflineMessages(String getterId) {
        return offLineDb.get(getterId);
    }

    public static void addOfflineMessages(String getterId, Message message) {
        offLineDb.get(getterId).add(message);
    }

    public static ConcurrentHashMap<String, ArrayList<Message>> getOffLineDb() {
        return offLineDb;
    }

    public static void removeOfflineMessages(String getterId, Message message) {
        offLineDb.get(getterId).remove(message);
    }

}
