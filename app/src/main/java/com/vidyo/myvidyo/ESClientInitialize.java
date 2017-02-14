package com.vidyo.myvidyo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by Administrator on 2017/2/10.
 */

public interface ESClientInitialize {
  public boolean ESClientInitialize(Activity activity, String CRTpath);
}
