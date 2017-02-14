package com.esoon.vidyosample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

//import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.esoon.utils.Contants;
import com.esoon.utils.Tools;

public class MainDemoActivity extends Activity  implements OnClickListener
{

	public static int MainImageIds[]=new int[]{R.drawable.dianshang_main ,R.drawable.baoxian_main,R.drawable.rukoubg};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		int currentapiVersion=android.os.Build.VERSION.SDK_INT;
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // disable title bar
		//改状态条的颜色, 必须是这种....===============================
		if(currentapiVersion >=19)
		{
			//android 4.4 以后才能支持.
//			Window window = getWindow();
//			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, 
//			WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//			window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, 
//			WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		
		setContentView(R.layout.activity_main_demo);
		
	/*	SystemBarTintManager tintManager = new SystemBarTintManager(this);
		tintManager.setStatusBarTintEnabled(true);
		tintManager.setNavigationBarTintEnabled(false);
		tintManager.setTintColor(Color.WHITE);*/
		
		
		ImageView img = (ImageView)this.findViewById(R.id.img_gocallmain);
		img.setOnClickListener(this);
		
		RelativeLayout lay = (RelativeLayout)this.findViewById(R.id.rlayout_top1);
		
		lay.setBackgroundResource(MainImageIds[Contants.MainWeb_Tag]);
		
		
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_demo, menu);
		return true;
	}


	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		if(v.getId()== R.id.img_gocallmain)
		{
			
			Intent it = new Intent(this,CallMainActivity.class);
			this.startActivity(it);
			
			//checkExceptionQuitRoom();
			
			
		}
		
	}
	
	@Override
	public void onWindowFocusChanged(final boolean hasWindowFocus)
	{
		super.onWindowFocusChanged(hasWindowFocus);
		if(hasWindowFocus && isFirst)
		{
			checkExceptionQuitRoom();
			isFirst = false;
			
		}
		
	}
	private boolean isFirst =true;
	private String LastRoomid = null;
	public void checkExceptionQuitRoom()
	{
		String lastroomid =Tools.getConfigData(this, "roomid");
		
		if(lastroomid !=null &&  !"".equals(lastroomid))
		{
			//移出保存的roomid . 
		Tools.SaveConfigData(this, "roomid", "");
		LastRoomid = lastroomid;	
		new AlertDialog.Builder(this).setTitle("系统提示")//设置对话框标题  
		  
	     .setMessage("您最近异常退出了房间:" + lastroomid +",是否重新进入?")//设置显示的内容  
	  
	     .setPositiveButton("进入",new DialogInterface.OnClickListener() {//添加确定按钮  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
	            // finish();
	             System.out.println ("alertdialog:确定"); 
	             //进入房间. 
	         	String roomid =LastRoomid;
				Intent it = new Intent(MainDemoActivity.this,VideoActivity.class);
				it.putExtra("roomid", roomid);
				MainDemoActivity.this.startActivity(it);
			     
	         }  
	  
	     }).setNegativeButton("取消",new DialogInterface.OnClickListener() {//添加返回按钮  
	         @Override  
	         public void onClick(DialogInterface dialog, int which) {//响应事件 
	        	 // finish();
	            System.out.println ("alertdialog:返回"); 
	         }  
	  
	     }).show();//在按键响应事件中显示此对话框  
		
		
		
		
		}
		
		
		
		
		
		
	  
	 }  
	  
		
	

	
}
