package com.pojo;

/**
 * Created by Administrator on 2017/2/9.
 */

public class LoginMessage {
    String  userName;
    String  userPsd;
    public LoginMessage(){}
    @Override
    public String toString() {
        return "LoginMessage{" +
                "userName='" + userName + '\'' +
                ", userPsd='" + userPsd + '\'' +
                '}';
    }

    public LoginMessage(String userName, String userPsd) {
        this.userName = userName;
        this.userPsd = userPsd;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPsd() {
        return userPsd;
    }

    public void setUserPsd(String userPsd) {
        this.userPsd = userPsd;
    }
}
