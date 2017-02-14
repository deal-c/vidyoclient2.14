package com.esoon.vidyosample;

import java.util.ArrayList;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.esoon.utils.Contants;
import com.esoon.utils.INetRequest;
import com.esoon.utils.Tools;

public class LoginActivity extends Activity implements OnClickListener,
		INetRequest,OnMenuItemClickListener,OnSoftKeyboardStateChangedListener
{
	private ProgressDialog progressDialog = null;

	//注册软键盘状态变化监听
	public void addSoftKeyboardChangedListener(OnSoftKeyboardStateChangedListener listener) {
	    if (listener != null) {
	        mKeyboardStateListeners.add(listener);
	    }
	}
	//取消软键盘状态变化监听
	public void removeSoftKeyboardChangedListener(OnSoftKeyboardStateChangedListener listener) {
	    if (listener != null) {
	        mKeyboardStateListeners.remove(listener);
	    }
	}

	private ArrayList<OnSoftKeyboardStateChangedListener> mKeyboardStateListeners;      //软键盘状态监听列表
	private OnGlobalLayoutListener mLayoutChangeListener;
	private boolean mIsSoftKeyboardShowing;
	
	
	/**
	 * 显示加载层.
	 * 
	 * @param msg
	 */
	public void showProgressDialog(String msg)
	{
		progressDialog = ProgressDialog.show(LoginActivity.this, "", msg, true,
				false);
	}

	/**
	 * 隐藏加载层.
	 */
	public void dismissProgressDialog()
	{
		if (progressDialog != null)
			progressDialog.dismiss();
		progressDialog = null;
	}

	TextView tv_manualpostion = null;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Button bntlogin = (Button) this.findViewById(R.id.button_login);

		TextView bnt_changetheme = (TextView) this.findViewById(R.id.bnt_changetheme);
		
		//=========test ===========
		tv_manualpostion = (TextView) this.findViewById(R.id.tv_manualpostion);
		
		bnt_changetheme.setOnClickListener(this);
		
		bntlogin.setOnClickListener(this);

		DisplayMetrics dis = Tools.getScreenSize(this);
		Contants.screen_width = dis.widthPixels;
		Contants.screen_height = dis.heightPixels;

		Contants.Status_Height = Tools.getScreenStatusHeight(this);

		System.out.println("Screen Size:" + dis.widthPixels + ":"
				+ dis.heightPixels + ":" + Contants.Status_Height);

		// 载入配置. 
		 String s = Tools.getConfigData(this, "maintag");
		 if(s!=null && !"".equals(s))
		 {
			 Contants.MainWeb_Tag = Integer.parseInt(s);
		 }
		
		 System.out.println ("MainWeb_Tag=" + 	 Contants.MainWeb_Tag);
		 
		 //========测试软键盘弹出. ======
		 mIsSoftKeyboardShowing = false;
		    mKeyboardStateListeners = new ArrayList<OnSoftKeyboardStateChangedListener>();
		    mLayoutChangeListener = new OnGlobalLayoutListener() {
		        @Override
		        public void onGlobalLayout() {
		            //判断窗口可见区域大小
		            Rect r = new Rect();
		            getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
		            //如果屏幕高度和Window可见区域高度差值大于整个屏幕高度的1/3，则表示软键盘显示中，否则软键盘为隐藏状态。
		            int heightDifference = Contants.screen_height - (r.bottom - r.top);
		            boolean isKeyboardShowing = heightDifference > Contants.screen_height/3;

		            //如果之前软键盘状态为显示，现在为关闭，或者之前为关闭，现在为显示，则表示软键盘的状态发生了改变
		            if ((mIsSoftKeyboardShowing && !isKeyboardShowing) || (!mIsSoftKeyboardShowing && isKeyboardShowing)) {
		                mIsSoftKeyboardShowing = isKeyboardShowing;
		                for (int i = 0; i < mKeyboardStateListeners.size(); i++) {
		                    OnSoftKeyboardStateChangedListener listener = mKeyboardStateListeners.get(i);
		                    listener.OnSoftKeyboardStateChanged(mIsSoftKeyboardShowing, heightDifference);
		                }
		            }
		        }
		    };
		    //注册布局变化监听
		    getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(mLayoutChangeListener);
		 
		    
		    //添加listener
		    addSoftKeyboardChangedListener(this);
		    
		    //载入配置
		    Tools.loadSetting(this);
		    
	}

	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus)
		{
			if (Tools.isNetworkConnected(this) == false)
			{
				Toast.makeText(this, "网络不通,获取数据失败", 3).show();
				return;
			}

		}

	}
	

	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		// MenuItem item = menu.getItem(0);

		return true;
	}

	private static String TAG = "MyLoginActivity";

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.bnt_changetheme:
			{
				PopupMenu popupMenu = new PopupMenu(LoginActivity.this, v); 
				popupMenu.setOnMenuItemClickListener(this);
			    getMenuInflater().inflate(R.menu.login, popupMenu.getMenu());  	
			    popupMenu.show();
				System.out.println("bnt_changetheme...");
				break;
			}
			
		case R.id.button_login:
			/*
			 * Intent it = new Intent(this, MainDemoActivity.class);
			 * this.startActivity(it); this.finish();
			 */
		{
			if (Tools.isNetworkConnected(this) == false)
			{
				Toast.makeText(this, "网络不通,获取数据失败", 3).show();
				return;
			}

			// this.showProgressDialog("网络请求中...");
			EditText editusername = (EditText) this
					.findViewById(R.id.edit_username);
			EditText editpass = (EditText) this
					.findViewById(R.id.edit_password);

			String username = editusername.getEditableText().toString();
			String pass = editpass.getEditableText().toString();

			Log.i(TAG, "username=" + username);
			if (username == null || username.isEmpty())
			{
				Toast.makeText(this, "账户不能为空", 3).show();
				return;
			}

			Contants.serveruser = username;
			Contants.serveruserpass = pass;
			Contants.innerUser = Contants.getVideoUser(Contants.serveruser);
			System.out.println("启用视频账户:" + Contants.innerUser);

			// 登录成功.
			// Log.i(TAG, "roomid:" + result.getString("roomid"));
			// Log.i(TAG, "owerid:" + result.getString("owerid"));
			// Log.i(TAG, "displayname:" + result.getString("displayname"));
			// Log.i(TAG, "extension:" + result.getString("extension"));

			Intent it = new Intent(this, MainDemoActivity.class);
			this.startActivity(it);
			this.finish();
			// Tools.NetLogin(this, Contants.portal, username, pass, this);

		}

			break;
		}

	}

	@Override
	public void NetExecutePost(JSONObject result, int resultcode, int commandid)
	{
		// TODO Auto-generated method stub
		this.dismissProgressDialog();
		try
		{
			if (resultcode == 1)
			{
				// 登录成功.
				Log.i(TAG, "roomid:" + result.getString("roomid"));
				Log.i(TAG, "owerid:" + result.getString("owerid"));
				Log.i(TAG, "displayname:" + result.getString("displayname"));
				Log.i(TAG, "extension:" + result.getString("extension"));

				Intent it = new Intent(this, MainDemoActivity.class);
				this.startActivity(it);
				this.finish();

			} else
			{

				Toast.makeText(this, "账户或密码不正确,登录失败", 3).show();

			}

			Log.i(TAG, resultcode + ":" + commandid);

		} catch (Exception se)
		{

		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item)
	{
		
		switch (item.getItemId()) {
		case R.id.menu_login_item0:
		{
			//电商行业
			Tools.SaveConfigData(this,"maintag","0");
			Contants.MainWeb_Tag = 0;
			
			break;
		}
		case R.id.menu_login_item1:
		{
			//保险行业
			Tools.SaveConfigData(this,"maintag","1");
			Contants.MainWeb_Tag = 1;
			break;
		}
		
		
		case R.id.menu_login_item2:
		{
			//保险行业
			Tools.SaveConfigData(this,"maintag","2");
			Contants.MainWeb_Tag = 2;
			break;
		}
		
		case R.id.menu_setting_item:
		{
			//其他参数设置
			Intent  it = new Intent(this,SettingActivity.class);
			this.startActivity(it);
			break;
		}
		
		
		}
		// TODO Auto-generated method stub
		return false;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	protected void onDestroy() {
	    //移除布局变化监听
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
	        getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(mLayoutChangeListener);
	    } else {
	        getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutChangeListener);
	    }
	    super.onDestroy();
	}
	
	
	@Override
	public void OnSoftKeyboardStateChanged(boolean isKeyBoardShow,
			int keyboardHeight)
	{

		System.out.println ("isKeyBoardShow:"+ isKeyBoardShow +" keyboardHeight:" + keyboardHeight);
		
		LayoutParams ps =(LayoutParams)tv_manualpostion.getLayoutParams();
		if(keyboardHeight !=0)
			ps.bottomMargin=keyboardHeight;
		else
			ps.bottomMargin=20;
				
		tv_manualpostion.setLayoutParams(ps);
		
		
	};
}
