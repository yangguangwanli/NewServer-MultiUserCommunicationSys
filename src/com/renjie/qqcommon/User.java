package com.renjie.qqcommon;

import java.io.Serializable;

/**
 * @author Renjie
 * @version 1.0
 * 表示一个用户信息，有用户名和密码两个属性，提供有参无参构造
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;//增强兼容性
    private String userId;//用户Id/用户名
    private String passwd;//用户密码


    public User() {}
    public User(String userId, String passwd) {
        this.userId = userId;
        this.passwd = passwd;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
}
