package com.vidyo.myvidyo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.esoon.vidyosample.R;


public class modifyRomMsgActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_rom_msg);

    }

    public void onClick(View view) {

        finish();
    }
}
