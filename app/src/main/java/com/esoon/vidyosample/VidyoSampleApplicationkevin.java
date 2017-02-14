package com.esoon.vidyosample;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import org.xutils.*;

public class VidyoSampleApplicationkevin extends android.app.Application {
	@Override
	public void onCreate() {
		super.onCreate();
		x.Ext.init(this);
		x.Ext.setDebug(org.xutils.BuildConfig.DEBUG);
	}
	public static final String TAG = "VidyoSample";
	static Handler hdlr;
	long address;	

	VideoActivity videoAct= null;
	
	public VideoActivity getVideoAct()
	{
		return videoAct;
	}

	public void setVideoAct(VideoActivity videoAct)
	{
		this.videoAct = videoAct;
	}

	public VidyoSampleApplicationkevin() {
		address = 0;
	}
	
	public VidyoSampleApplicationkevin(Handler h) {
		hdlr = h;
		address = 0;
	}

	public void setHandler(Handler h) {
		hdlr = h;
	}

	private String getAndroidInternalMemDir()  throws IOException {
		File fileDir = getFilesDir(); //crashing
		if (fileDir != null) {
			String filedir=fileDir.toString() + "/";
			Log.d(TAG, "file directory = " + filedir);
			return filedir;
		} else {
			Log.e(TAG, "Something went wrong, filesDir is null");
		}
		return null;
	}

	private String getAndroidCacheDir() throws IOException{
		File cacheDir = getCacheDir();
		if (cacheDir != null) {
			String filedir=cacheDir.toString() + "/";
			return filedir;
		}
		return null;
	}
	
	/**
	* This function is temporary until when we start using internal memory and cache
	*/
	private String getAndroidSDcardMemDir() throws IOException{
	    File sdCard = Environment.getExternalStorageDirectory();
	    File dir = new File (sdCard.getAbsolutePath() + "/VidyoMobile");
	    dir.mkdirs();
	   
	    String sdDir = dir.toString() + "/";
	    return sdDir;
	}
	
	public boolean initialize(String caFileName, Activity activity) {
		String pathDir;
		try {
			pathDir = getAndroidInternalMemDir();
//			pathDir = getAndroidSDcardMemDir();
		} catch (Exception e) {
			e.printStackTrace();
			pathDir = "/data/data/com.esoon.vidyosample/app_marina/";
		}

		String logDir;
		try {
			logDir = getAndroidCacheDir();
//			logDir = getAndroidSDcardMemDir();
		} catch (Exception e) {
			e.printStackTrace();
			logDir = "/data/data/com.esoon.vidyosample/app_marina/";
		}
		System.out.println("？？？"+caFileName);
		System.out.println("？？？"+logDir);
		System.out.println("？？？"+pathDir);
		System.out.println("？？？"+activity);

	address = Construct(caFileName,logDir, pathDir, activity);
		if (address == 0)
			return false;
		else
			return true;
	}
	
	public void uninitialize() {
		Dispose();
	}
	public void cameraSwitchCallback(String name) {
		Message msg = Message.obtain();
		msg.obj = name;
		msg.setTarget(hdlr);
		msg.what = VideoActivity.SWITCH_CAMERA;
		msg.sendToTarget();	
	}

	public void messageBox(String s) {
//		Bundle b = new Bundle();
//		b.putString("text", s);
//		Mymessage m = Mymessage.obtain();
//		m.what = VideoActivity.MSG_BOX;
//		m.setData(b);
//		m.setTarget(hdlr);
//		m.sendToTarget();
	}

	public void GroupChat(String msg,String displayname,String uri) {
		Bundle b = new Bundle();
		b.putString("uri", uri);
		b.putString("msg", msg);
		b.putString("displayname", displayname);
		
		Message m = Message.obtain();
		m.what = VideoActivity.Event_Group_Chat;
		m.setData(b);
		m.setTarget(hdlr);
		m.sendToTarget();
	
	}
	
	
	public void callEndedCallback() {	
		Log.d(TAG, "Call ended received!");
		Message msg = Message.obtain();
		msg.setTarget(hdlr);
		msg.what = VideoActivity.CALL_ENDED;
		msg.sendToTarget();		
	}
	
	public void callStartedCallback() {	
		Log.d(TAG, "Call started received!");
		Message msg = Message.obtain();
		msg.setTarget(hdlr);
		msg.what = VideoActivity.CALL_STARTED;
		msg.sendToTarget();		
	}

	public void loginSuccessfulCallback() {	
		Log.d(TAG, "Login Successful received!");
		Message msg = Message.obtain();
		msg.setTarget(hdlr);
		msg.what = VideoActivity.LOGIN_SUCCESSFUL;
		msg.sendToTarget();		
	}

	public void libraryStartedCallback() {
		Log.d(TAG, "Library started received!");
		Message msg = Message.obtain();
		msg.setTarget(hdlr);
		msg.what = VideoActivity.LIBRARY_STARTED;
		msg.sendToTarget();
	}

	
	public void Callback() {	
		Log.d(TAG, "Login Successful received!");
		Message msg = Message.obtain();
		msg.setTarget(hdlr);
		msg.what = VideoActivity.LOGIN_SUCCESSFUL;
		msg.sendToTarget();		
	}
	
	//设置外放
	public  void AutoStartSpeaker(boolean autoStart)
	{
		AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);    
		if(autoStart)
		{
			audioManager.setMicrophoneMute(false);            
			audioManager.setSpeakerphoneOn(autoStart);//使用扬声器外放，即使已经插入耳机    
		}
		else
			audioManager.setSpeakerphoneOn(autoStart);
		//setVolumeControlStream(AudioManager.STREAM_MUSIC);//控制声音的大小    
		//audioManager.setMode(AudioManager.STREAM_MUSIC);
		
		
	}

	/*
	 * Native methods that are implemented by the 'VidyoSample' native library,
	 */
	public native long Construct(String caFileName, String logDir, String pathDir, Activity activity);

	public native void Dispose();
	
	public native void AutoStartMicrophone(boolean autoStart);
	public native void AutoStartCamera(boolean autoStart);
	
	public native void Login(String vidyoportalName, String userName, String passwordName);
	
	public native void Render();
	public native void RenderRelease();
	public native void HideToolBar(boolean disablebar);

    public native void SetCameraDevice(int camera);
    public native void SetPreviewModeON(boolean pip);

	public native void DisableAutoLogin();

	public native void Resize(int width, int height);
	
	public native void SendChat(String s);

	public native int SendAudioFrame(byte[] frame, int numSamples,
			int sampleRate, int numChannels, int bitsPerSample);

	public native int GetAudioFrame(byte[] frame, int numSamples,
			int sampleRate, int numChannels, int bitsPerSample);

	public native int SendVideoFrame(byte[] frame, String fourcc, int width,
			int height, int orientation, boolean mirrored);
	
	public native void TouchEvent(int id, int type, int x, int y);
	public native void SetOrientation(int orientation);

	public native void MuteCamera(boolean muteCamera);
	public native void DisableAllVideoStreams();
	public native void EnableAllVideoStreams();
	public native void StartConferenceMedia();
	public native void SetEchoCancellation (boolean aecenable);
	public native void SetSpeakerVolume (int volume);
	public native void DisableShareEvents();
	public native String getBandInfo();
	
	
	// load the library - name matches jni/Android.mk
	static {
	    System.loadLibrary("VidyoClientApp");
	    System.loadLibrary("ndkVidyoSample");
	}
}
