package com.esoon.vidyosample;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esoon.utils.Contants;
import com.esoon.utils.Contants.NetCommand;
import com.esoon.utils.INetRequest;
import com.esoon.utils.Tools;
import com.vidyo.myvidyo.CreateRomActivity;
import com.vidyo.myvidyo.MyLoginActivity;
import com.vidyo.myvidyo.modifyRomMsgActivity;

public class CallMainActivity extends Activity implements OnClickListener,
		INetRequest
{
	private static String TAG="callMain";
	private final int Msg_NetInvite = 1;
	private LinearLayout view_notifylist= null;
	

	// 邀请进入房间的提示窗口.
	Dialog conferenceDialog = null;
	boolean isStopLoop=false;
	/**
	 * 
	 * 启动循环获取邀请消息
	 * 
	 */
	public void LoopInviteMessage()
	{
		if(isStopLoop ) return ;
		Message msg = new Message();
		msg.what = Msg_NetInvite;
		hd.sendMessageDelayed(msg, 5000);

	}



	/**
	 * 显示邀请窗口
	 * 
	 * @param oval
	 */
	public void showInviteDialog(JSONObject oval)
	{

		conferenceDialog = Tools.createInviteDialog(this, this, oval);
		conferenceDialog.show();

		conferenceDialog.getWindow().setLayout(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call_main);
		Button bntservice = (Button) this.findViewById(R.id.bnt_callservice);
		Button bntmanager = (Button) this.findViewById(R.id.bnt_callmanager);
		Button	createRom=(Button)this.findViewById(R.id.createRom);
		TextView tx_nihao = (TextView) this.findViewById(R.id.tx_nihao);
		tx_nihao.setText(Contants.serveruser+",您好");
		createRom.setOnClickListener(this);
		bntservice.setOnClickListener(this);
		bntmanager.setOnClickListener(this);
		Button	btngal=(Button)findViewById(R.id.btngal);
		btngal.setOnClickListener(this);
		view_notifylist =(LinearLayout) this.findViewById(R.id.view_notifylist);
		view_notifylist.setOnClickListener(this);
		
		
		//请求通知会议列表.
		HashMap mp = new HashMap();
		mp.put("operation", "notifyVidyoRoom.action");
		mp.put("userid", Contants.serveruser);
		Tools.NetGetData(Contants.serverurl, mp, CallMainActivity.this,
				CallMainActivity.this, NetCommand.Notifylist.getValue());

		
	}

	/**
	 * 
	 * 创建一行的现实view
	 * 
	 * @param o
	 */
	private View createRowView(JSONObject o )
	{
		View v = LayoutInflater.from(this).inflate(
				R.layout.callrow_include, null);

		TextView text_opentime = (TextView)v.findViewById(R.id.text_opentime);
		TextView text_organizerrow = (TextView)v.findViewById(R.id.text_organizerrow);
		TextView text_topicrow = (TextView)v.findViewById(R.id.text_topicrow);
		ImageButton bnt_attend = (ImageButton)v.findViewById(R.id.bnt_attend);
		bnt_attend.setOnClickListener(this);
		
		try
		{
			bnt_attend.setTag(o.getString("roomid"));
			text_opentime.setText(o.getString("startdate") +"-" + o.getString("enddate"));
			text_topicrow.setText(o.getString("roomtopic"));
			text_organizerrow.setText("组织者:"+o.getString("roomOrganizer"));
			
		}catch(Exception se)
		{
			
		}
		return v;
		
	}
	
	
	Handler hd = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Bundle b = msg.getData();
			switch (msg.what) {
			case Msg_NetInvite:
			{
				HashMap mp = new HashMap();
				mp.put("operation", "inviteVidyoRoom.action");
				mp.put("userid", Contants.serveruser);
				Tools.NetGetData(Contants.serverurl, mp, CallMainActivity.this,
						CallMainActivity.this, NetCommand.InviteRoom.getValue());
			}

				break;
			}

		}
	};

	@Override
	public void onWindowFocusChanged(final boolean hasWindowFocus)
	{
		super.onWindowFocusChanged(hasWindowFocus);
		if(hasWindowFocus)
		{
			isStopLoop =false;
			LoopInviteMessage();
		}
		else
		{
			isStopLoop =true;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		
		ImageButton img = null;
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.call_main, menu);
		return true;
	}

	private Dialog dialogselectalltype  = null;
	private String calltype ="";
	@Override
	public void onClick(View v)
	{
		
		
		switch (v.getId()) {
		case R.id.bnt_selectvideoroom:
		{
			System.out.println("bnt_selectvideoroom click ...");
			dialogselectalltype.dismiss();
			if(calltype.equals("service"))
			{
				//客服中心
				Tools.showProgressDialog(this, "加载中...");
				HashMap mp = new HashMap();
				// operation=&userid=cust2
				mp.put("operation", "addVidyoRoom.action");
				mp.put("userid", Contants.serveruser);
				mp.put("subMeidaType", "vidyoVideo");
				mp.put("type", "1");
				mp.put("managerId", Contants.managerId);
	
				Tools.NetGetData(Contants.serverurl,
						mp, this, this, NetCommand.CreateRoomService.getValue());
				
			}else
			{
				// 经理
				Tools.showProgressDialog(this, "加载中...");
				HashMap mp = new HashMap();
				// operation=&userid=cust2
				mp.put("operation", "addVidyoRoom.action");
				mp.put("userid", Contants.serveruser);
				mp.put("subMeidaType", "vidyoVideo");
				mp.put("type", "2");
				mp.put("managerId", Contants.managerId);
	
				Tools.NetGetData(Contants.serverurl,
						mp, this, this, NetCommand.CreateRoomManager.getValue());
			}
			break;
		}
			case R.id.btngal:
Intent	intent=new Intent(CallMainActivity.this, MyLoginActivity.class);
				startActivity(intent);
				finish();
			break;
		case R.id.bnt_attend:
		{
			//参加会议按钮点击处理
			String roomid = v.getTag().toString();
			Intent it = new Intent(this,VideoActivity.class);
			it.putExtra("roomid", roomid);
			this.startActivity(it);
			
			sendErrorMsg("房间号:"+roomid+"不存在", roomid);
			
			break;
		}
		
		case R.id.bnt_callservice:
		{
			
			dialogselectalltype=Tools.createSelectCallType(this, this);
			dialogselectalltype.show();
			dialogselectalltype.getWindow().setLayout(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			calltype ="service";

		}
			break;
		case R.id.bnt_callmanager:
		{
			dialogselectalltype=Tools.createSelectCallType(this, this);
			dialogselectalltype.show();
			dialogselectalltype.getWindow().setLayout(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			calltype ="manager";

//			
		}
			break;
			// 邀请窗口, 拒绝, 接受.
			case R.id.bnt_jujue:
			{
				Log.i(TAG,"拒绝点击");
				
				conferenceDialog.dismiss();
				LoopInviteMessage();
				break;
			}
			case R.id.bnt_jieshou:
			{
				Log.i(TAG,"接受点击");
				
				conferenceDialog.dismiss();
				//LoopInviteMessage();
				String roomid = v.getTag().toString();
				Intent it = new Intent(this,VideoActivity.class);
				it.putExtra("roomid", roomid);
				this.startActivity(it);
				
				sendErrorMsg("房间号:"+roomid+"不存在", roomid);
				
				break;
			}
			case R.id.createRom:
			{
				Intent	intent1intent=new Intent(CallMainActivity.this,CreateRomActivity.class);
				startActivity(intent1intent);
			}
			break;
			case R.id.view_notifylist:
				Intent	bintent=new Intent(CallMainActivity.this,modifyRomMsgActivity.class);
				startActivity(bintent);
				break;


		}

	}

	@Override
	public void NetExecutePost(JSONObject result, int resultcode, int commandid)
	{
		Log.i(TAG, "resultcode =" + resultcode +" commandid=" + commandid);
		
		//================================================================================
		//创建房间
		if(commandid == NetCommand.CreateRoomManager.getValue()
				|| commandid == NetCommand.CreateRoomService.getValue())
		{
			try
			{
		//	Tools.showProgressDialog(this, "加载中...");
			Tools.dismissProgressDialog();
			if(resultcode ==1)
			{
				int retcode = result.getInt("RetCode");
				if(retcode ==0)
				{
					
					String roomid = result.getString("roomid");
					
					Intent it = new Intent(this,VideoActivity.class);
					it.putExtra("roomid", roomid);
					it.putExtra("showqueue", true);
					it.putExtra("needdelete", true);
					if(commandid == NetCommand.CreateRoomManager.getValue())
					{
						it.putExtra("calltype","manager");
						
					}
					if(commandid == NetCommand.CreateRoomService.getValue())
					{
						it.putExtra("calltype","service");
						
					}
					this.startActivity(it);
					
					sendErrorMsg("房间号:"+roomid+"不存在", roomid);
					
					
				}else
				{
					// 创建房间失败, 显示失败原因
					String ErrorMsg = result.getString("ErrorMsg");
				//	Toast.makeText(this,ErrorMsg, 3).show();
				}
			}
			}catch(Exception se)
			{}
		}
		// 通知会议列表. 
		if(commandid == NetCommand.Notifylist.getValue())
		{
			try
			{
				JSONArray jarr = result.getJSONArray("conference");


				for(int i=0;i<jarr.length();i++)
				{


					JSONObject otemp = jarr.getJSONObject(i);
					View vtemp  = this.createRowView(otemp);
					view_notifylist.addView(vtemp);

				}

				
				//模拟数据. 
				/**
				 * 
				 * bnt_attend.setTag(o.getString("roomid"));
				text_opentime.setText(o.getString("startdate") +"-" + o.getString("enddate"));
				text_topicrow.setText(o.getString("roomtopic"));
				text_organizerrow.setText("组织者:"+o.getString("roomOrganizer"));
				 */
				
				///===============test ==============
//				JSONObject ob = new JSONObject();
//				ob.put("roomid", "73");
//				ob.put("startdate", "2016");
//				ob.put("enddate", "2016");
//				ob.put("roomtopic", "测试模拟的房间");
//				ob.put("roomOrganizer", "陈宝吉");
//				View vtemp  = this.createRowView(ob);
//				view_notifylist.addView(vtemp);
				
				
				
			}catch(Exception se)
			{
				
			}
			
			
			
		}
		//================================================================================
		// TODO Auto-generated method stub
		if (commandid == NetCommand.InviteRoom.getValue())
		{
			// 收到"邀请进入房间" 数据包
			if (conferenceDialog != null && conferenceDialog.isShowing())
			{
				// 已经有弹出提示了. 忽略邀请信息.
				//LoopInviteMessage();
				Log.i(TAG,"正在显示对话窗口...");
				return;
			}

			if (resultcode == 1)
			{
				// 获取数据包.
				try
				{
					
					Object oval = result.get("conference");
					//数据 为空的时候 格式:{conference:""}
					
					if (oval instanceof JSONObject)
					{
						this.showInviteDialog((JSONObject)oval);

					} else
					{
						// 没拿到数据.
						LoopInviteMessage();
					}

				} catch (Exception se)
				{
					se.printStackTrace();
				}

			} else
			{
				// 没拿到数据.
				LoopInviteMessage();
			}

		}

	}
	
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		try
		{
			switch (item.getItemId()) {
			case R.id.menu_test_enterroom:
			{
				JSONObject ob = new JSONObject();
				ob.put("roomid", "73");
				ob.put("startdate", "2016");
				ob.put("enddate", "2016");
				ob.put("roomtopic", "测试模拟的房间");
				ob.put("roomOrganizer", "陈宝吉");
				View vtemp  = this.createRowView(ob);
				view_notifylist.addView(vtemp);
				
				break;
			}
		
		}
		}catch(Exception se){}
		return  true;
		
	}
	
	/**
	 * 发送错误信息.
	 * @param msg
	 * @param roomid
	 */
	public void sendErrorMsg(String msg, String roomid)
	{
		HashMap<String,Object> mp = new HashMap<String,Object>();
		mp.put("operation", "sendErrorMsg.action");
		mp.put("userid", Contants.serveruser);
		mp.put("roomid", roomid);
		mp.put("errorCode", "10001");
		mp.put("errorMsg", msg);
		mp.put("errorAbstract",msg);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		mp.put("occurTime",df.format(new Date()));
		
		mp.put("phoneModel", android.os.Build.MODEL);
		mp.put("vidyoVersion", "3.3");
		
		Tools.NetGetData(Contants.serverurl,
				mp, this, this, NetCommand.ErrorMsg.getValue());
		
		
	}
	private View createmyRowView(JSONObject	o) {
		View v = LayoutInflater.from(this).inflate(
				R.layout.create, null);
		Button	createRom=(Button)findViewById(R.id.createRom);
		createRom.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent	intent=new Intent(CallMainActivity.this, CreateRomActivity.class);
				startActivity(intent);
			}
		});




		return v;
	}


}
