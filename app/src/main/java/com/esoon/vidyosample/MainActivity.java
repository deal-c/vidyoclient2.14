package com.esoon.vidyosample;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends FragmentActivity implements OnClickListener {

	private FragmentManager manager;
	private FragmentTransaction tran;
	private RadioGroup radioGroup;

	private boolean isShowLogin = false;

	Dialog LoginDialog = null;

	public static void Log(String... object) {
		StringBuffer sb = new StringBuffer(1024);
		for (String s : object) {
			sb.append(s);
		}
		Log.d("com.vidyo.vidyosample", sb.toString());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		manager = getSupportFragmentManager();
		tran = manager.beginTransaction();
		tran.replace(R.id.fragment, new Fragment1());
		tran.commit();

		radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(RadioGroup group, int checkedId) {
				tran = manager.beginTransaction();
				switch (checkedId) {
				case R.id.tab_one:
					tran.replace(R.id.fragment, new Fragment1());
					break;
				case R.id.tab_two:
					 tran.replace(R.id.fragment, new Fragment2());
					break;
				case R.id.tab_three:
					// tran.replace(R.id.fragment, new Fragment3());
					break;

				case R.id.tab_four:
					// tran.replace(R.id.fragment, new Fragment3());
					break;

				}
				tran.commit();
			}
		});

	}

	/**
	 * 窗口显示的时候触发 显示登录窗口.
	 */
	public void onWindowFocusChanged(boolean hasFocus) {
		Log("onWindowFocusChanged...");
		synchronized (this) {
			if (isShowLogin)
				return;
			isShowLogin = true;
			LoginDialog = createLoginDialog();
			LoginDialog.show();
		}
	}

	/**
	 * 创建登录窗口.
	 * 
	 * @return
	 */
	private Dialog createLoginDialog() {
		Dialog ret = null;
		View loginview = LayoutInflater.from(this).inflate(
				R.layout.activity_login, null);
		Button bntlogin = (Button) loginview.findViewById(R.id.button_login);
		ret = new AlertDialog.Builder(this).setTitle("登录").setView(loginview)
				.create();
		bntlogin.setOnClickListener(this);

	//	Button bntexit = (Button) loginview.findViewById(R.id.Button_exit);

		//bntexit.setOnClickListener(this);

		return ret;

		// return ret;
	}

	public void onClick(View v) {
	

		switch (v.getId()) {
	
			
		case R.id.button_login:
			if (LoginDialog != null) {
				LoginDialog.dismiss();
				LoginDialog = null;
			}
			break;
			
		}

	}
}
