package com.vidyo.myvidyo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.esoon.vidyosample.R;


public class CreateRomActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);



    }

    public void onClick(View view) {
        finish();
    }
}
