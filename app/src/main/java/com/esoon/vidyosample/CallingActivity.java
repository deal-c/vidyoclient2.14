package com.esoon.vidyosample;

import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.esoon.utils.Contants;
import com.esoon.utils.Contants.NetCommand;
import com.esoon.utils.INetRequest;
import com.esoon.utils.Tools;

public class CallingActivity extends Activity implements OnClickListener,
		INetRequest
{
	public static final int Msg_NextRequest = 10;
	public static final int Msg_cancel = 11;
	public static final int Msg_ok = 12;
	public boolean isStop = false;
	
	TextView tx_mancount = null;

	private Handler hd = new Handler()
	{

		public void handleMessage(Message msg)
		{
			switch (msg.what) {
			case Msg_NextRequest:
				if (isStop)
					return;
				HashMap mp = new HashMap();
				mp.put("operation", "queueNumber.action");
				mp.put("userid", Contants.serveruser);

				Tools.NetGetData(Contants.serverurl, mp, CallingActivity.this,
						CallingActivity.this, NetCommand.QueueNumber.getValue());
				break;
			case Msg_cancel:
			{
				CallingActivity.this.setResult(RESULT_CANCELED);
				CallingActivity.this.finish();
			}
				break;
			case Msg_ok:
			{
				CallingActivity.this.setResult(RESULT_OK);
				CallingActivity.this.finish();
				break;
			}

			}

		}
	};

	private  String CallingType ="";
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.CallingType = this.getIntent().getStringExtra("calltype");
		
		setContentView(R.layout.activity_calling);

		tx_mancount = (TextView) this.findViewById(R.id.tx_mancount);

		TextView tx_username = (TextView) this.findViewById(R.id.tx_username);
		tx_username.setText(Contants.serveruser);

		Button bt = (Button) this.findViewById(R.id.bnt_cancelcall);
		bt.setOnClickListener(this);

		VidyoSampleApplicationkevin app = (VidyoSampleApplicationkevin) getApplication();
		app.getVideoAct().setMHandler(hd);

	
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		isStop = true;
	}

	@Override
	public void onWindowFocusChanged(final boolean hasWindowFocus)
	{
		super.onWindowFocusChanged(hasWindowFocus);
		if (hasWindowFocus)
		{
			if ("service".equalsIgnoreCase(CallingType))
			{
				Message mm = Message.obtain();
				mm.what = Msg_NextRequest;
				hd.sendMessage(mm);
			} else
			{
				this.tx_mancount.setText("状态:等待客户经理接听...");
			}

		} else
		{
			isStop = true;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calling, menu);
		return true;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		if (v.getId() == R.id.bnt_cancelcall)
		{
			hd.sendEmptyMessage(this.Msg_cancel);

		}
	}

	@Override
	public void NetExecutePost(JSONObject result, int resultcode, int commandid)
	{
		// TODO Auto-generated method stub
		if (resultcode == 1)
		{
			try
			{
				int RetCode = result.getInt("RetCode");
				if (RetCode == 0)
				{
					// 成功.
					int qnum = result.getInt("queueNum");
					this.tx_mancount.setText("状态:还有" + qnum + "人排队中...");

				} else
				{
					String emsg = result.getString("ErrorMsg");
					this.tx_mancount.setText("状态:" + emsg);
				}
			} catch (Exception se)
			{
			}
		}
		Message mm = Message.obtain();
		mm.what = Msg_NextRequest;
		hd.sendMessageDelayed(mm, 3000);

	}

}
