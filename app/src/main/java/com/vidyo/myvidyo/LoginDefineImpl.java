package com.vidyo.myvidyo;

/**
 * Created by Administrator on 2017/2/11.
 */

public class LoginDefineImpl    implements LoginDefine {
    @Override
    public boolean logindefine(String   userName,String userPsd) {

        LoginInterface  loginInterface=new LoginImpl();

        return loginInterface.LoginMessage(userName,userPsd);
    }
}
