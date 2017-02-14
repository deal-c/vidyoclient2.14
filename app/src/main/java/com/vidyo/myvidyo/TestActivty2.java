package com.vidyo.myvidyo;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.esoon.utils.Contants;
import com.esoon.vidyosample.R;
import com.esoon.vidyosample.VidyoSampleApplicationkevin;

public class TestActivty2 extends Activity implements View.OnClickListener{

    public static final int startLogin = 1;
    public static final int startJoin = 2;
    VidyoSampleApplicationkevin app;
    ESClientJoinRoom    esClientJoinRoom=new ESClientJoinRoomImpl();
    String  crtpath="F:\\vidyoClient\\app\\src\\main\\res\\raw\\ca_certificates.crt";
    ESClientInitialize  esClientInitialize=new ESClientInitializeImpl();
Button  Join_button;
    Button  login_button;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_activty2);
        app = (VidyoSampleApplicationkevin) getApplication();

        appSampleHttp.Arguments args = new appSampleHttp.Arguments("192.168.5.47","test3","123456",TestActivty2.this);
        AsyncTask<appSampleHttp.Arguments, Integer, appSampleHttp.Arguments> atHttpCalls = new appSampleHttp().execute( args );
        System.out.println("？？？？进程死了么");
        Join_button=(Button)findViewById(R.id.Join_button);
        login_button=(Button)findViewById(R.id.login_button);
        Join_button.setOnClickListener(this);
        login_button.setOnClickListener(this);




    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.login_button:
            esClientInitialize.ESClientInitialize(TestActivty2.this,crtpath);
            esClientJoinRoom.joinRom(TestActivty2.this,"123");
                   break;
            case R.id.Join_button:

                break;
            default:
                    break;


    }}
}
