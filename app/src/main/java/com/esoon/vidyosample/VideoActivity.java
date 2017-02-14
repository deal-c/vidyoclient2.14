package com.esoon.vidyosample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.esoon.utils.Contants;
import com.esoon.utils.Contants.NetCommand;
import com.esoon.utils.EnterRoomHttp;
import com.esoon.utils.ICancelCall;
import com.esoon.utils.INetRequest;
import com.esoon.utils.Tools;
import com.vidyo.LmiDeviceManager.LmiDeviceManagerView;
import com.vidyo.LmiDeviceManager.LmiVideoCapturer;


public class VideoActivity extends Activity implements
		LmiDeviceManagerView.Callback, SensorEventListener,
		View.OnClickListener,INetRequest,ICancelCall,OnSoftKeyboardStateChangedListener
		,OnSizeChangedListener
{
	private static final String TAG = "videoactivity";
	private boolean doRender = false;
	private LmiDeviceManagerView bcView; // new 2.2.2
	private boolean bcCamera_started = false;
	private  boolean loginStatus = false;
	private boolean cameraPaused = false;
	private boolean cameraStarted = false;
	public static final int CALL_ENDED = 0;
	public static final int MSG_BOX = 1;
	public static final int CALL_RECEIVED = 2;
	public static final int CALL_STARTED = 3;
	public static final int SWITCH_CAMERA = 4;
	public static final int LOGIN_SUCCESSFUL = 5;
	public static final int LIBRARY_STARTED = 6;
	public static final int Event_Group_Chat = 7;
	public static final int Event_ShowBand = 10;
	
	final float degreePerRadian = (float) (180.0f / Math.PI);
	final int ORIENTATION_UP = 0;
	final int ORIENTATION_DOWN = 1;
	final int ORIENTATION_LEFT = 2;
	final int ORIENTATION_RIGHT = 3;
	private float[] mGData = new float[3];
	private float[] mMData = new float[3];
	private float[] mR = new float[16];
	private float[] mI = new float[16];
	private float[] mOrientation = new float[3];

	final int DIALOG_LOGIN = 0;
	final int DIALOG_JOIN_CONF = 3;
	final int DIALOG_MSG = 1;
	final int DIALOG_CALL_RECEIVED = 2;
	final int FINISH_MSG = 4;

	VidyoSampleApplicationkevin app;
	StringBuffer message;
	private int currentOrientation;
	private SensorManager sensorManager;

	int usedCamera = 1;

	
	//==========================================
	String roomid="";  // 需要进入的房间id
	boolean  needdelete =false; // 退出房间后, 是否要删除
	boolean showqueue =false; // 进入房间后, 是否需要排队. 
	private ArrayList <HashMap> groupmsglist = new ArrayList <HashMap>(); // 存储消息数据
	boolean hasStartVideo =false;
	Handler MHandler= null;
	Dialog queuedialog  = null;
	private String calltype ="";  //呼叫类型,  客服中心or 客户经理
	RadioButton bntGuanduan = null;
	
	boolean isCloseMicro =false;		//是否关闭了 麦克. 
	boolean isCloseSpeaker =false;  // 是否关闭了 扬声器
	boolean isFullscreen =false;  // 是否全屏. 
	boolean isCloseCamare =false;		//是否关闭了采集视频
	
	RadioGroup  panelView = null;
	TextView tv_title_video= null;
	ImageView bnt_exitfullscreen = null;
	LinearLayout view_messagelist= null; // 聊天内容面板. 
	LinearLayout  panel_chatsend= null; // 发送聊天内容面板. 
	EditText edit_chatmsg = null;
	TextView text_bandinfo = null; //显示带宽信息
	private boolean isloopband=true;

	com.esoon.vidyosample.InputMethodRelativeLayout input_layout ;
	
	//=======================================
	
	public Handler getMHandler()
	{
		return MHandler;
	}

	public void setMHandler(Handler mHandler)
	{
		MHandler = mHandler;
	}

	private boolean mIsOnPause = false;
	private ImageView cameraView;
	private AudioManager audioManager;



	private String getAndroidSDcardMemDir() throws IOException
	{
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard.getAbsolutePath() + "/VidyoMobile");
		dir.mkdirs();

		String sdDir = dir.toString() + "/";
		return sdDir;
	}

	private String getAndroidInternalMemDir() throws IOException
	{
		File fileDir = getFilesDir(); // crashing
		if (fileDir != null)
		{
			String filedir = fileDir.toString() + "/";
			Log.d(TAG, "file directory = " + filedir);
			return filedir;
		} else
		{
			Log.e(TAG, "Something went wrong, filesDir is null");
		}
		return null;
	}

	
//	
	@Override
	protected void onNewIntent(Intent intent) 
	{
		super.onNewIntent(intent);
		System.out.println ("onNewIntent....");
		
	}

	
	
	
	private String writeCaCertificates()
	{
		try
		{
			InputStream caCertStream = getResources().openRawResource(
					R.raw.ca_certificates);
			// File caCertFileName;
			// caCertFileName = getFileStreamPath("ca-certificates.crt");

			File caCertDirectory;
			try
			{
				String pathDir = getAndroidInternalMemDir();
				caCertDirectory = new File(pathDir);
			} catch (Exception e)
			{
				caCertDirectory = getDir("marina", 0);
			}
			File cafile = new File(caCertDirectory, "ca-certificates.crt");

			FileOutputStream caCertFile = new FileOutputStream(cafile);
			byte buf[] = new byte[1024];
			int len;
			while ((len = caCertStream.read(buf)) != -1)
			{
				caCertFile.write(buf, 0, len);
			}
			caCertStream.close();
			caCertFile.close();

			return cafile.getPath();
			
		} catch (Exception e)
		{
			e.printStackTrace();
			return null;
			
		}
	}

	/**
	 * 开始显示视频
	 */
	public void startDislayVideo()
	{
		if(hasStartVideo) return ;
		Log.d(TAG,"启动视频");
		app.StartConferenceMedia();
		app.SetPreviewModeON(true);
		app.SetCameraDevice(usedCamera);
		app.DisableShareEvents();
		startDevices();
		hasStartVideo =true;
		app.AutoStartSpeaker(true);
		setupAudio();
		
	}
	
	private void startBandShow()
	{
			isloopband = true;
			Message mm = new Message();
			mm.what = VideoActivity.Event_ShowBand;
			message_handler.sendMessageDelayed(mm, 5000);
		
	}

	
	private void stopBandShow()
	{
		isloopband =false;
	}
	
	 Handler message_handler = new Handler()
	{
		public void handleMessage(Message msg)
		{

			Bundle b = msg.getData();
			System.out.println ("?????????????????:" + msg);
			switch (msg.what) 
			{


			case Event_ShowBand:
			{
//				//tests ================
//					Rect r = new Rect();
//					//bcView.getWindowVisibleDisplayFrame(outRect)
//					input_layout.getWindowVisibleDisplayFrame(r);
//		            //如果屏幕高度和Window可见区域高度差值大于整个屏幕高度的1/3，则表示软键盘显示中，否则软键盘为隐藏状态。
//		            int heightDifference = Contants.screen_height - (r.bottom - r.top);
//		            boolean isKeyboardShowing = heightDifference > Contants.screen_height/3;
//
//		            System.out.println ("bottom:"+r.bottom +" top:"+ r.top + "heightDifference======="+heightDifference +" Contants.screen_height ==" + Contants.screen_height );
				
				String info = app.getBandInfo();
				if(info !=null)
				{
					String stra [] = info.split(",");
					//if(!"0".equals(stra[0]) && !"0".equals(stra[1]))
					{
						String showinfo = "收:"+stra[0]+"/秒\n" + "发:"+stra[1]+"/秒";
						text_bandinfo.setText(showinfo);
						System.out.println (showinfo);
					}
					
				}
				if(isloopband)
				{
					Message mm = new Message();
					mm.what = VideoActivity.Event_ShowBand;
					message_handler.sendMessageDelayed(mm, 5000);
				}
				
				
				break;
			}
		
			case Event_Group_Chat:
				//保存消息到队列
				//截取消息中最后的<br>
				String msgdata = b.getString("msg");
				if(msgdata !=null)
				{
					int pos = msgdata.lastIndexOf("<");
					if(pos !=-1)
					{
						msgdata = msgdata.substring(0, pos);
					}
				}
				
				System.out.println ("收到消息:" + msgdata);
				HashMap hsmsg = new HashMap();
				hsmsg.put("msg", b.getString("msg"));
				hsmsg.put("uri", b.getString("uri"));
				hsmsg.put("displayname", b.getString("displayname"));
				//显示聊天内容到屏幕
				ShowMessage(b.getString("displayname"), msgdata);
				groupmsglist.add(hsmsg);
				
				if(groupmsglist.size() ==1)
				{
					// 第一条消息的时候, 启动视频画面
					
						if(VideoActivity.this.getMHandler() !=null)
						{
							Log.d(TAG,"收到短信,  开启视频");
							VideoActivity.this.getMHandler().sendEmptyMessage(CallingActivity.Msg_ok);
						}
					
					
				}
				//显示聊天信息...
				
				break;
				
			case LIBRARY_STARTED:
				app.DisableAutoLogin();
				break;

			case CALL_STARTED:
				dismissProgressDialog();// 隐藏加载层. 
				//if(videoWait==false)
				{
					//进入房间后, 不立即显示视频, 等待指令.
					startDislayVideo();
					
			
				}
				break;

			case CALL_ENDED:
				stopDevices();
				VideoActivity.this.finish();
				
				app.RenderRelease();
				break;

			case MSG_BOX:
				message = new StringBuffer(b.getString("text"));
				//showDialog(DIALOG_MSG);
				break;

			case SWITCH_CAMERA:
				String whichCamera = (String) (msg.obj);
				boolean isFrontCam = whichCamera.equals("FrontCamera");
				Log.d(VidyoSampleApplicationkevin.TAG, "Got camera switch = "
						+ whichCamera);
				
				break;

			case LOGIN_SUCCESSFUL:
				//showDialog(DIALOG_JOIN_CONF);
				// 调用进入房间接口. 
				{
					Log.d(TAG,"登陆成功");
					EnterRoomHttp.Arguments args = new EnterRoomHttp.Arguments(
							Contants.portal, Contants.innerUser,
							Contants.innerPass,roomid, VideoActivity.this);
					AsyncTask<EnterRoomHttp.Arguments, Integer, EnterRoomHttp.Arguments> atHttpCalls = new EnterRoomHttp()
							.execute(args);
				}
				break;
			}
		}
	};
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		//不锁屏.
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		Log.d(TAG, "entering onCreate " );
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE); // disable title bar
		//必须传递roomid 这个参数. 
		this.roomid = this.getIntent().getStringExtra("roomid");
		this.needdelete = this.getIntent().getBooleanExtra("needdelete", false);
		this.showqueue =this.getIntent().getBooleanExtra("showqueue", false);

		this.calltype = this.getIntent().getStringExtra("calltype");

		
		app = (VidyoSampleApplicationkevin) getApplication();
		app.setHandler(message_handler);
		app.setVideoAct(this);
		
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
				WindowManager.LayoutParams.FLAG_FULLSCREEN
						| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		
		setContentView(R.layout.conference);

		bcView = new LmiDeviceManagerView(this, this);
		View C = findViewById(R.id.glsurfaceview);
		ViewGroup parent = (ViewGroup) C.getParent();
		int index = parent.indexOfChild(C);
		parent.removeView(C);
		parent.addView(bcView, index);
	
		// 设置大小变化监控.
		input_layout = (InputMethodRelativeLayout)this.findViewById(R.id.input_layout);
		input_layout.setOnSizeChangedListener(this);
		
		 bntGuanduan = (RadioButton)this.findViewById(R.id.tab_guanduan_video);
		 bntGuanduan.setOnClickListener(this);
		 
		 
		 panelView = (RadioGroup)this.findViewById(R.id.radioGroupview);
		 
		 tv_title_video = (TextView) findViewById(R.id.tv_title_video);
		 
		 RadioButton t = (RadioButton)this.findViewById(R.id.tab_fullscreen_video);
		 t.setOnClickListener(this);
		 
		 t = (RadioButton)this.findViewById(R.id.tab_micro_video);
		 t.setOnClickListener(this);
		 t = (RadioButton)this.findViewById(R.id.tab_speaker_video);
		 t.setOnClickListener(this);
		 t = (RadioButton)this.findViewById(R.id.tab_camera_video);
		 t.setOnClickListener(this);
		 t = (RadioButton)this.findViewById(R.id.tab_chat_video);
		 t.setOnClickListener(this);
	
		 
		 view_messagelist = (LinearLayout)this.findViewById(R.id.view_messagelist);
		 
		 panel_chatsend  =(LinearLayout)this.findViewById(R.id.panel_sendmsg);
		 
//		cameraView = (ImageView) findViewById(R.id.action_camera_icon);
//		cameraView.setOnClickListener(this);

		 text_bandinfo = (TextView) findViewById(R.id.text_bandinfo);
		 bnt_exitfullscreen=(ImageView) findViewById(R.id.bnt_exitfullscreen);
		 bnt_exitfullscreen.setOnClickListener(this);
		 
		 edit_chatmsg = (EditText)panel_chatsend.findViewById(R.id.chat_editmsg);
		 Button bnt_sendchatmsg = (Button) panel_chatsend.findViewById(R.id.bnt_sendchatmsg);
		 bnt_sendchatmsg.setOnClickListener(this);
		 
		 edit_chatmsg.setOnEditorActionListener(new TextView.OnEditorActionListener() {  
             
	            @Override  
	            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {  
	                /*判断是否是“GO”键*/  
	            	System.out.println ("actionId:" + actionId);
	            	
	                if(actionId == EditorInfo.IME_ACTION_DONE )
	                {  
	                    /*隐藏软键盘*/  
	                    InputMethodManager imm = (InputMethodManager) v  
	                            .getContext().getSystemService(  
	                                    Context.INPUT_METHOD_SERVICE);  
	                    if (imm.isActive()) { 
	                    	
	                        imm.hideSoftInputFromWindow(  
	                                v.getApplicationWindowToken(), 0);  
	                    }
	                      
	                    return true;  
	                }
	                return false;  
	            } 
	        });  
//		 
		 
		/* Camera */
		usedCamera = 1;

	//	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		//NetworkInfo netInfo = cm.getActiveNetworkInfo();
		String caFileName = writeCaCertificates();
		String dialogMessage;
		setupAudio(); // will set the audio to high volume level

		currentOrientation = -1;

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor gSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		Sensor mSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		sensorManager.registerListener(this, gSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);

		if (! Tools.isNetworkConnected(this))
		{
			dialogMessage = new String("Network Unavailable!\n"
					+ "Check network connection.");
			showErrorDialog("网络不通");
			// app = null;
			return;
		} else if (app.initialize(caFileName, this) == false)
		{
			dialogMessage = new String("Initialization Failed!\n"
					+ "Check network connection.");
			showErrorDialog("网络不通,initialize 错误");
			// app = null;
			return;
		}

		if (!loginStatus)
		{

			StartVideoServerLogin();
			loginStatus = true;
			app.HideToolBar(true);
			app.SetEchoCancellation(true);
		}
		
		app.HideToolBar(true);
		//显示排队界面. 
		if(showqueue)
		{
			Log.d(TAG,"显示排队界面...");
			Intent it = new Intent(this,CallingActivity.class);
			
			it.putExtra("calltype", calltype);
			
			this.startActivityForResult(it, 1);
		}
		
		//存储异常退出标记
		Tools.SaveConfigData(this, "roomid", roomid);
		
		
		    
		
		Log.d(TAG, "leaving onCreate");
		
		
	}
	
	/**
	 * 监控键盘按键事件
	 */
	@Override
	 public boolean dispatchKeyEvent(KeyEvent event) {  
//	        System.out.println ("event.getKeyCode():" + event.getKeyCode());
//		 if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
//	        { 
//	            /*隐藏软键盘*/  
//	            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
//	            if(inputMethodManager.isActive()){  
//	                inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);  
//	            }
//	         
//	            return true;  
//	        }  
	        return super.dispatchKeyEvent(event);  
	    }
	/**
	 * 显示聊天内容到界面. 
	 * @param man
	 * @param msg
	 */
	public void ShowMessage(String man, String msg)
	{
		
		//view_messagelist
		LayoutInflater inf = LayoutInflater.from(this);
		TextView v = (TextView)inf.inflate(R.layout.chat_message, null);
		v.setText(man +":" + msg);
		if(view_messagelist !=null)
			view_messagelist.addView(v);
		
	}
	
	@Override
	protected  void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.d(TAG,"onActivityResult " + resultCode +" requestCode:" + requestCode);

		if(resultCode == Activity.RESULT_CANCELED)
		this.finish();
	}
	private void setupAudio()
	{
		int set_Volume = 45535;
		app.SetSpeakerVolume(set_Volume);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		Log.d(TAG, "onPause Begin");

		if(hasStartVideo ==false)
			return ;
	
		LmiVideoCapturer.onActivityPause();
		mIsOnPause = true;
		pauseCall();
		if (cameraStarted)
		{
			cameraPaused = true;
			cameraStarted = false;
		} else
		{
			cameraPaused = false;
		}
		app.DisableAllVideoStreams();

		Log.d(TAG, "onPause End");
		//app.EnableAllVideoStreams();
	}

	
	@Override
	public void onStop()
	{
		super.onStop();
		Log.i(TAG, "onStop");
//		dismissProgressDialog();// 隐藏加载层. 
//		
//		stopDevices();
//		app.DisableAllVideoStreams();
//		app.Dispose();
//		app.uninitialize();
		
		
	}
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.i(TAG, "onDestroy");
		dismissProgressDialog();// 隐藏加载层. 
		
		stopDevices();
		app.DisableAllVideoStreams();
		app.Dispose();
		app.uninitialize();
		
		//删除房间.
		if(needdelete)
		{
			Log.d(TAG,"delete Rooom ...");
			
			HashMap mp = new HashMap();
			mp.put("operation", "deleteVidyoRoom.action");
			mp.put("userid", Contants.serveruser);
			mp.put("roomid", this.roomid);

			Tools.NetGetData(Contants.serverurl,
					mp, this.getApplicationContext(), null, NetCommand.DeleteRoom.getValue());

		}
		
	    
		
	}

	@Override
	public void onResume()
	{
		super.onResume();
		Log.d(TAG, "onResume Begin");
		if(hasStartVideo ==false)
			return ;
		mIsOnPause = false;
		
		resumeCall();
		app.EnableAllVideoStreams();
		Log.d(TAG, "onResume End");
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		
		//清除异常退出标记
		Tools.SaveConfigData(this, "roomid", "");
		stopDevices();
		app.DisableAllVideoStreams();
		app.Dispose();
		app.uninitialize();
		finish();
	}

	void startDevices()
	{
		doRender = true;
		startBandShow(); // 循环显示带宽
		app.AutoStartSpeaker(true);
		setupAudio();
	}

	
	void stopDevices()
	{
		stopBandShow();  //停止刷新带宽
		doRender = false;
	}

	private void resumeCall()
	{
		Log.d(TAG,"调用:resumeCall ");
		this.bcView.onResume();
	}

	private void pauseCall()
	{
		this.bcView.onPause();
	}
	
	/**
	 * 显示加载中...
	 */
	private ProgressDialog progressDialog = null;

	public void showProgressDialog(String msg)
	{
		progressDialog = ProgressDialog.show(VideoActivity.this, "",
				msg, true, false);
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

	@Override
	public void onWindowFocusChanged(final boolean hasWindowFocus)
	{
		super.onWindowFocusChanged(hasWindowFocus);
		Log.d(TAG, "ACTIVITY ON WINDOW FOCUS CHANGED "
				+ (hasWindowFocus ? "true" : "false"));
		if (hasWindowFocus && !mIsOnPause  && this.hasStartVideo)
		{
			Log.d(TAG, " EnableAllVideoStreams resumeCall ..onWindowFocusChanged..");
			resumeCall();
			app.EnableAllVideoStreams();
		}
	}
	
	//============显示错误信息=========================================
	/**
	 * 显示错误信息
	 */
	AlertDialog alerterror=null;
	private void showErrorDialog(String msg)
	{
		
		
		AlertDialog.Builder builder;
		stopDevices();

		builder = new AlertDialog.Builder(this).setTitle(msg)
				.setPositiveButton("OK",
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog,
									int whichButton)
							{
								alerterror.dismiss();
								// showDialog(DIALOG_JOIN_CONF);
								finish();
							}
						});
		alerterror = builder.create();
		alerterror.show();
		
	}

	private void StartVideoServerLogin()
	{
		showProgressDialog("加载中...");
		app.Login("http://" + Contants.portal, Contants.innerUser,
				Contants.innerPass);
		
	}
	
	public void LmiDeviceManagerViewRender()
	{
		if (doRender)
		{
			//System.out.println ("app.Render()");
			app.Render();
		}
	}

	public void LmiDeviceManagerViewResize(int width, int height)
	{
		app.Resize(width, height);
	}

	public void LmiDeviceManagerViewRenderRelease()
	{
		app.RenderRelease();
	}

	public void LmiDeviceManagerViewTouchEvent(int id, int type, int x, int y)
	{
		app.TouchEvent(id, type, x, y);
	}

	public int LmiDeviceManagerCameraNewFrame(byte[] frame, String fourcc,
			int width, int height, int orientation, boolean mirrored)
	{
		return app.SendVideoFrame(frame, fourcc, width, height, orientation,
				mirrored);
	}

	public int LmiDeviceManagerMicNewFrame(byte[] frame, int numSamples,
			int sampleRate, int numChannels, int bitsPerSample)
	{
		return app.SendAudioFrame(frame, numSamples, sampleRate, numChannels,
				bitsPerSample);
	}

//	public int LmiDeviceManagerSpeakerNewFrame(byte[] frame, int numSamples,
//			int sampleRate, int numChannels, int bitsPerSample)
//	{
//		return app.GetAudioFrame(frame, numSamples, sampleRate, numChannels,
//				bitsPerSample);
//	}

	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}

	public void onSensorChanged(SensorEvent event)
	{
		int newOrientation = currentOrientation;

		int type = event.sensor.getType();
		float[] data;
		if (type == Sensor.TYPE_ACCELEROMETER)
		{
			data = mGData; /* set accelerometer data pointer */
		} else if (type == Sensor.TYPE_MAGNETIC_FIELD)
		{
			data = mMData; /* set magnetic data pointer */
		} else
		{
			return;
		}
		/* copy the data to the appropriate array */
		for (int i = 0; i < 3; i++)
			data[i] = event.values[i]; /*
										 * copy the data to the appropriate
										 * array
										 */

		/*
		 * calculate the rotation data from the latest accelerometer and
		 * magnetic data
		 */
		Boolean ret = SensorManager.getRotationMatrix(mR, mI, mGData, mMData);
		if (ret == false)
			return;

		SensorManager.getOrientation(mR, mOrientation);

		Configuration config = getResources().getConfiguration();
		boolean hardKeyboardOrientFix = (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO);

		int pitch = (int) (mOrientation[1] * degreePerRadian);
		int roll = (int) (mOrientation[2] * degreePerRadian);

		if (pitch < -45)
		{
			if (hardKeyboardOrientFix)
				newOrientation = ORIENTATION_LEFT;
			else
				newOrientation = ORIENTATION_UP;
		} else if (pitch > 45)
		{
			if (hardKeyboardOrientFix)
				newOrientation = ORIENTATION_RIGHT;
			else
				newOrientation = ORIENTATION_DOWN;
		} else if (roll < -45 && roll > -135)
		{
			if (hardKeyboardOrientFix)
				newOrientation = ORIENTATION_UP;
			else
				newOrientation = ORIENTATION_RIGHT;
		} else if (roll > 45 && roll < 135)
		{
			if (hardKeyboardOrientFix)
				newOrientation = ORIENTATION_DOWN;
			else
				newOrientation = ORIENTATION_LEFT;
		}

	
		// + " roll: " + roll);
		if (newOrientation != currentOrientation)
		{
			currentOrientation = newOrientation;
			app.SetOrientation(newOrientation);
		}

		/*
		 * if (newOrientation != currentOrientation) {
		 * camera.setCameraOrientation( newOrientation ); currentOrientation =
		 * newOrientation; }
		 */
	}

	@Override
	public void onClick(View arg0)
	{
		
		switch (arg0.getId()) {
		case R.id.bnt_sendchatmsg:
		{
			String chatmsg = this.edit_chatmsg.getText().toString();
			System.out.println ("发送聊天内容..." +chatmsg);
			
			if(chatmsg.isEmpty())
			{
				Toast.makeText(this, "发送内容不能为空", 3).show();
			}else
			{
				app.SendChat(chatmsg);
				this.ShowMessage("我:", chatmsg);
				edit_chatmsg.setText("");
				
				 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		            // 隐藏软键盘
		         imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
			}
			break;
		}
		
		case R.id.bnt_cancelcall:
		{
			queuedialog.dismiss();
			resumeCall();
			app.EnableAllVideoStreams();
			startDislayVideo();
			
		}
			break;
			
		case R.id.tab_guanduan_video:
		{
			// hand off .
			this.onBackPressed();
			break;
		}
		case R.id.bnt_exitfullscreen:
		{
			// 退出全屏. 
			this.panelView.setVisibility(View.VISIBLE);
			bnt_exitfullscreen.setVisibility(View.GONE);
			this.tv_title_video.setVisibility(View.VISIBLE);
			break;
		}
		case R.id.tab_fullscreen_video:
		{
				System.out.println ("全屏....");
				this.panelView.setVisibility(View.GONE);
				bnt_exitfullscreen.setVisibility(View.VISIBLE);
				this.tv_title_video.setVisibility(View.GONE);
			
				break;
			
		}
		case R.id.tab_chat_video:
		{
			//聊天开启,关闭
			//app.SendChat("888888888发送测试消息....");
			if(	panel_chatsend.getVisibility() == View.GONE)
				panel_chatsend.setVisibility(View.VISIBLE);
			else
				panel_chatsend.setVisibility(View.GONE);
				
			break;
		}
		case R.id.tab_camera_video:
		{
			//摄像头开启,关闭
			this.isCloseCamare = !this.isCloseCamare;
			app.AutoStartCamera(isCloseCamare);
			RadioButton rv = (RadioButton)arg0;
			
			Drawable top = null;
			if(this.isCloseCamare)
				top = getResources().getDrawable(R.drawable.guanbishipin_1);
			else
				top = getResources().getDrawable(R.drawable.guanbishipin);
			
			rv.setCompoundDrawablesWithIntrinsicBounds(null, top , null, null);
			
			break;
		}
		
		case R.id.tab_micro_video:
		{
			//麦克开启,关闭
			this.isCloseMicro = !this.isCloseMicro;
			app.AutoStartMicrophone(isCloseMicro);
			
			Drawable top = null;
			RadioButton rv = (RadioButton)arg0;
			
			if(this.isCloseMicro)
				top = getResources().getDrawable(R.drawable.jingying_1);
			else
				top = getResources().getDrawable(R.drawable.jingying);
			
			rv.setCompoundDrawablesWithIntrinsicBounds(null, top , null, null);
			
			
			
			break;
		}
		case R.id.tab_speaker_video:
		{
			//声音外放开启,关闭
			this.isCloseSpeaker = !this.isCloseSpeaker;
			app.AutoStartSpeaker(isCloseSpeaker);
			
			Drawable top = null;
			RadioButton rv = (RadioButton)arg0;
			
			if(this.isCloseSpeaker)
				top = getResources().getDrawable(R.drawable.yangshenqi_1);
			else
				top = getResources().getDrawable(R.drawable.yangshenqi);
			
			rv.setCompoundDrawablesWithIntrinsicBounds(null, top , null, null);
			
			
			
			break;
		}
		
		//case R.id.action_camera_icon:
//			if (usedCamera == 1)
//			{
//				usedCamera = 0;
//			} else
//			{
//				usedCamera = 1;
//			}
//			app.SetCameraDevice(usedCamera);

			/*
			 * if (bcCamera.isStarted()) { if (bcCamera.useFrontCamera) {
			 * bcCamera.switchCamera(false, false, 0, false, false);
			 * app.SetCameraDevice(1);
			 * cameraView.setImageResource(R.drawable.icon_back_camera); } else
			 * { bcCamera.switchCamera(true, false, 0, false, false);
			 * app.SetCameraDevice(0);
			 * cameraView.setImageResource(R.drawable.icon_front_camera); } }
			 */
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		System.out.println ("屏幕旋转了in onConfigurationChanged " +newConfig.orientation);
	}

	@Override
	public void NetExecutePost(JSONObject result, int resultcode, int commandid)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnCancelCall()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void OnSoftKeyboardStateChanged(boolean isKeyBoardShow,
			int keyboardHeight)
	{

		System.out.println ("isKeyBoardShow:"+ isKeyBoardShow +" keyboardHeight:" + keyboardHeight);
		
		LayoutParams ps =(LayoutParams)panel_chatsend.getLayoutParams();
		if(isKeyBoardShow )
			ps.bottomMargin=keyboardHeight;
		else
			ps.bottomMargin=80;
				
		panel_chatsend.setLayoutParams(ps);
		
		
	}
	@Override
	public void onSizeChange(boolean flag)
	{
		// TODO Auto-generated method stub
		System.out.println ("onSizeChange.....");
		
	};
}
