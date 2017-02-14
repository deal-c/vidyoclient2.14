package com.vidyo.myvidyo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.view.WindowManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.esoon.utils.Contants;
import com.esoon.utils.EnterRoomHttp;
import com.esoon.vidyosample.R;

import com.esoon.vidyosample.VideoActivity;
import com.esoon.vidyosample.VidyoSampleApplicationkevin;
import com.vidyo.LmiDeviceManager.*;

import android.hardware.SensorManager;

public class ESClientInitializeImpl  implements ESClientInitialize,
        LmiDeviceManagerView.Callback,
        SensorEventListener {

    private static final String TAG = "VidyoSampleActivity";

    private boolean doRender = false;

    private LmiDeviceManagerView bcView; // new 2.2.2
    private boolean bcCamera_started = false;
    private static boolean loginStatus = false;
    private boolean cameraPaused = false;
    private boolean cameraStarted = false;
    public static final int CALL_ENDED = 0;
    public static final int MSG_BOX = 1;
    public static final int CALL_RECEIVED = 2;
    public static final int CALL_STARTED = 3;
    public static final int SWITCH_CAMERA = 4;
    public static final int LOGIN_SUCCESSFUL = 5;
    public static final int LIBRARY_STARTED = 6;
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
    Handler message_handler;
    StringBuffer message;
    private int currentOrientation;
    private SensorManager sensorManager;
    StringBuffer serverString;
    StringBuffer usernameString;
    StringBuffer passwordString;
    public static boolean isHttps = false;
    String portaAddString;
    String guestNameString;
    String roomKeyString;
    int usedCamera = 1;

    private boolean mIsOnPause = false;
    private ImageView cameraView;
    private AudioManager audioManager;

    EnterRoomHttp.Arguments args=null;


private boolean flag=false;



    @Override
    public boolean ESClientInitialize(final Activity activity,String   crtpath) {


        message_handler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                System.out.print("1111111111111111111111111111111111" + msg);
                switch (msg.what) {
                    case LIBRARY_STARTED:
                        app.DisableAutoLogin();
                        break;

                    case CALL_STARTED:
                        app.StartConferenceMedia();
                        app.SetPreviewModeON(true);
                        app.SetCameraDevice(usedCamera);
                        app.DisableShareEvents();
                        startDevices();
                        break;

                    case CALL_ENDED:
                        stopDevices();
                        activity.showDialog(DIALOG_JOIN_CONF);
                        app.RenderRelease();
                        break;

                    case MSG_BOX:
                        message = new StringBuffer(b.getString("text"));
                        activity.showDialog(DIALOG_MSG);
                        break;

                    case SWITCH_CAMERA:
                        String whichCamera = (String) (msg.obj);
                        boolean isFrontCam = whichCamera.equals("FrontCamera");
                        Log.d(VidyoSampleApplication.TAG, "Got camera switch = " + whichCamera);

                        // switch to the next camera, force settings are per device.
                        // sample does not get this values
                        //	bcCamera.switchCamera(isFrontCam, false, 0, false, false);
                        break;

                    case LOGIN_SUCCESSFUL:
                        activity.showDialog(DIALOG_JOIN_CONF);
                        break;
                }
            }
        };

//	  app = new VidyoSampleApplication(message_handler);
        app = (VidyoSampleApplicationkevin) activity.getApplication();
        app.setHandler(message_handler);

        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);// get the full screen size from android
        System.out.print("222222222222222222222222222");


        usedCamera = 1;

        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        String dialogMessage;
//        setupAudio(); // will set the audio to high volume level

        currentOrientation = -1;

        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        Sensor gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        System.out.print("33333333333333333333333333");
        if (netInfo == null || !netInfo.isConnected()) {
            dialogMessage = new String("Network Unavailable!\n" + "Check network connection.");
            activity.showDialog(FINISH_MSG);
            //app = null;
            return  flag;
        } else if (app.initialize(crtpath, activity) == false) {
            dialogMessage = new String("Initialization Failed!\n" + "Check network connection.");
            activity.showDialog(FINISH_MSG);
            //app = null;
            return  flag;
        }
        if (!loginStatus) {
            activity.showDialog(DIALOG_LOGIN);
            loginStatus = true;
            app.HideToolBar(false);
            app.SetEchoCancellation(true);
        }
        Log.d(TAG, "leaving onCreate");
        flag=true;



        return flag;


    }

    private void setupAudio() {
        int set_Volume = 65535;
        app.SetSpeakerVolume(set_Volume);
    }


    void startDevices() {
        doRender = true;
    }

    void stopDevices() {
        doRender = false;
    }

    private void pauseCall() {
        this.bcView.onPause();
    }


    public void LmiDeviceManagerViewRender() {
        if (doRender)
            app.Render();
    }

    public void LmiDeviceManagerViewResize(int width, int height) {
        app.Resize(width, height);
    }

    public void LmiDeviceManagerViewRenderRelease() {
        app.RenderRelease();
    }

    public void LmiDeviceManagerViewTouchEvent(int id, int type, int x, int y) {
        app.TouchEvent(id, type, x, y);
    }

    public int LmiDeviceManagerCameraNewFrame(byte[] frame, String fourcc,
                                              int width, int height, int orientation, boolean mirrored) {
        return app.SendVideoFrame(frame, fourcc, width, height, orientation, mirrored);
    }

    public int LmiDeviceManagerMicNewFrame(byte[] frame, int numSamples,
                                           int sampleRate, int numChannels, int bitsPerSample) {
        return app.SendAudioFrame(frame, numSamples, sampleRate, numChannels,
                bitsPerSample);
    }

    public int LmiDeviceManagerSpeakerNewFrame(byte[] frame, int numSamples,
                                               int sampleRate, int numChannels, int bitsPerSample) {
        return app.GetAudioFrame(frame, numSamples, sampleRate, numChannels,
                bitsPerSample);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
       /* int newOrientation = currentOrientation;

        int type = event.sensor.getType();
        float[] data;
        if (type == Sensor.TYPE_ACCELEROMETER) {
            data = mGData; *//* set accelerometer data pointer *//*
        } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            data = mMData; *//* set magnetic data pointer *//*
        } else {
            return;
        }
        *//* copy the data to the appropriate array *//*
        for (int i = 0; i < 3; i++)
            data[i] = event.values[i];		*//* copy the data to the appropriate array *//*

		*//*
		 * calculate the rotation data from the latest accelerometer and
		 * magnetic data
		 *//*
        Boolean ret = SensorManager.getRotationMatrix(mR, mI, mGData, mMData);
        if (ret == false)
            return;

        SensorManager.getOrientation(mR, mOrientation);

       Configuration config =getResources().getConfiguration();
        boolean hardKeyboardOrientFix = (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO);

        int pitch = (int) (mOrientation[1] * degreePerRadian);
        int roll = (int) (mOrientation[2] * degreePerRadian);

       if (pitch < -45) {
            if (hardKeyboardOrientFix)
                newOrientation = ORIENTATION_LEFT;
            else
                newOrientation = ORIENTATION_UP;
        } else if (pitch > 45) {
            if (hardKeyboardOrientFix)
                newOrientation = ORIENTATION_RIGHT;
            else
                newOrientation = ORIENTATION_DOWN;
        } else if (roll < -45 && roll > -135) {
            if (hardKeyboardOrientFix)
                newOrientation = ORIENTATION_UP;
            else
                newOrientation = ORIENTATION_RIGHT;
        } else if (roll > 45 && roll < 135) {
            if (hardKeyboardOrientFix)
                newOrientation = ORIENTATION_DOWN;
            else
                newOrientation = ORIENTATION_LEFT;
        }

        if (newOrientation != currentOrientation) {
            currentOrientation = newOrientation;
            app.SetOrientation(newOrientation);
        }
*/

    }


}
