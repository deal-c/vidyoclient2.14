package com.esoon.utils;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.esoon.vidyosample.R;

public class Tools
{

	private static ProgressDialog progressDialog = null;

	public static void showProgressDialog(Context context, String msg)
	{
		progressDialog = ProgressDialog.show(context, "",
				msg, true, false);
		
		progressDialog.show();
		
	}
	
	public static void dismissProgressDialog()
	{
		if(progressDialog !=null)
			progressDialog.dismiss();
		progressDialog= null;
		
	}
	/**
	 * 获取网络数据包, 转换为json对象.
	 * 
	 * @param url
	 * @param mp
	 * @return
	 */
	public static JSONObject NetGetData(String baseUrl,
			HashMap<String, Object> mp, Context context, INetRequest callback,
			int commandid)
	{
		JSONObject ret = null;
		if (isNetworkConnected(context) == false)
		{
			Toast.makeText(context, "网络不通,获取数据失败", 3).show();
			return null;
		}

		JsonHttp.Arguments args = new JsonHttp.Arguments(baseUrl, mp, context,
				callback, commandid);

		AsyncTask<JsonHttp.Arguments, Integer, JsonHttp.Arguments> atHttpCalls = new JsonHttp()
				.execute(args);

		return ret;
	}

	/**
	 * 网络登录接口
	 * @param context
	 * @param baseUrl
	 * @param username
	 * @param passwd
	 * @param callback
	 * @return
	 */
	
	public static JSONObject NetLogin(Context context,String baseUrl,
			String username, String passwd, INetRequest callback)
	{
		JSONObject ret = null;
		if (isNetworkConnected(context) == false)
		{
			Toast.makeText(context, "网络不通,获取数据失败", 3).show();
			return null;
		}

		LoginHttp.Arguments args = new LoginHttp.Arguments(baseUrl,username ,passwd,
				callback);

		AsyncTask<LoginHttp.Arguments, Integer, LoginHttp.Arguments> atHttpCalls = new LoginHttp()
				.execute(args);

		return ret;
	}
	
	
	public static Dialog createQueueDialog(Activity _this,
			OnClickListener _click, JSONObject oval)
	{
		AlertDialog ret = null;
		View loginview = LayoutInflater.from(_this).inflate(
				R.layout.activity_calling, null);
		
		//tx_mancount = (TextView) this.findViewById(R.id.tx_mancount);

		TextView tx_username = (TextView) loginview.findViewById(R.id.tx_username);
		tx_username.setText(Contants.serveruser);

		Button bt = (Button) loginview.findViewById(R.id.bnt_cancelcall);
		bt.setOnClickListener(_click);
		
	
		
		/*
		 * ret = new Dialog(_this, R.style.mydialog);
		 * ret.setContentView(loginview);
		 */
		AlertDialog.Builder builder = new AlertDialog.Builder(_this);
		ret = builder.create();
		ret.setView(loginview, 0, 0, 0, 0);
		return ret;
	}
	
	
	/**
	 * 创建参加会议的提示创建.
	 * @param _this
	 * @param _click
	 * @return
	 */
	public static Dialog createInviteDialog(Activity _this,
			OnClickListener _click, JSONObject oval)
	{
		AlertDialog ret = null;
		View loginview = LayoutInflater.from(_this).inflate(
				R.layout.dialog_conference_msg, null);
		TextView tv_roomname = (TextView)loginview.findViewById(R.id.text_roomname);
		TextView tv_roomtopic= (TextView)loginview.findViewById(R.id.text_roomtopic);
		TextView t_organ = (TextView)loginview.findViewById(R.id.text_organizer);
		try
		{
			tv_roomname.setText(oval.getString("roomname"));
			tv_roomtopic.setText(oval.getString("roomtopic"));
			t_organ.setText(oval.getString("roomOrganizer"));
			Button  bnt_jieshou= (Button)loginview.findViewById(R.id.bnt_jieshou);
			bnt_jieshou.setTag(oval.getString("roomid"));
			
			Button  bntjujue= (Button)loginview.findViewById(R.id.bnt_jujue);
			
			bnt_jieshou.setOnClickListener(_click);
			bntjujue.setOnClickListener(_click);
			
			
		}catch(Exception se)
		{
			se.printStackTrace();
		}
			
		
		
		
		/*
		 * ret = new Dialog(_this, R.style.mydialog);
		 * ret.setContentView(loginview);
		 */
		AlertDialog.Builder builder = new AlertDialog.Builder(_this);
		ret = builder.create();
		ret.setView(loginview, 0, 0, 0, 0);
		return ret;
	}

	
	
	public static Dialog createSelectCallType(Activity _this,
			OnClickListener _click)
	{
		AlertDialog ret = null;
		View loginview = LayoutInflater.from(_this).inflate(
				R.layout.dialog_selectroomtype, null);
		Button bnt_selectvideoroom = (Button)loginview.findViewById(R.id.bnt_selectvideoroom);
		
		bnt_selectvideoroom.setOnClickListener(_click);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(_this);
		ret = builder.create();
		ret.setView(loginview, 0, 0, 0, 0);
		return ret;
	}

	public static int getScreenStatusHeight(Activity act)
	{

		Rect rect = new Rect();
		act.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
		return rect.top;

	}

	// 屏幕的宽度, 高度.
	public static DisplayMetrics getScreenSize(Activity act)
	{

		DisplayMetrics displayMetrics = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics;
	}

	/**
	 * 检测网络是否连通...
	 * 
	 * @param context
	 * @return true: 连通, false: No connected..
	 */
	public static boolean isNetworkConnected(Context context)
	{
		if (context != null)
		{
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null)
			{
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * wifi 是否可用.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiConnected(Context context)
	{
		if (context != null)
		{
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (mWiFiNetworkInfo != null)
			{
				return mWiFiNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 手机移动网络是否可用.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMobileConnected(Context context)
	{
		if (context != null)
		{
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mMobileNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mMobileNetworkInfo != null)
			{
				return mMobileNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	/**
	 * 获取网络的连接方式
	 * 
	 * @param context
	 * @return -1: 没任何连接.
	 */
	public static int getConnectedType(Context context)
	{
		if (context != null)
		{
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null && mNetworkInfo.isAvailable())
			{
				return mNetworkInfo.getType();
			}
		}
		return -1;
	}

	/**
	 * 读写配置文件.
	 * @param context
	 * @param key
	 * @param sdata
	 */
	public static void SaveConfigData(Context context,String key ,String sdata)
	{
		
	     SharedPreferences.Editor editor = context.getSharedPreferences("lock", context.MODE_PRIVATE).edit();
         editor.putString(key, sdata);
         editor.commit();
	}
	

	public static String getConfigData(Context context,String key)
	{
		
		SharedPreferences read = context.getSharedPreferences("lock", context.MODE_PRIVATE);
		String value = read.getString(key, "");
		return value;  
	}
	
	
	
	public static void loadSetting(Context context)
	{
		SharedPreferences read = context.getSharedPreferences("lock", context.MODE_PRIVATE);
		String sval = read.getString("servervideo", "");
		if(sval!=null && !"".equals(sval))
		{
			Contants.portal = sval;
		}
		
		sval = read.getString("server360", "");
		if(sval!=null && !"".equals(sval))
		{
			Contants.serverurl = sval;
		}
		
		sval = read.getString("managerId", "");
		if(sval!=null && !"".equals(sval))
		{
			Contants.managerId = sval;
		}
		 System.out.println ("loadSetting:" + Contants.portal + "--"+Contants.serverurl );
		      
	}

	
	public static void SaveSetting(Context context,String servervideo ,String server360 , String managerId)
	{
	     SharedPreferences.Editor editor = context.getSharedPreferences("lock", context.MODE_PRIVATE).edit();
         editor.putString("servervideo", servervideo);
         editor.putString("server360", server360);
         editor.putString("managerId", managerId);
         
         Contants.serverurl = server360;
         Contants.portal = servervideo;
         Contants.managerId = managerId;
         
         
         System.out.println ("SaveSetting:" + servervideo + "--"+server360 );
         editor.commit();
	}
}
