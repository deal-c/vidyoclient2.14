package com.esoon.vidyosample;

import android.app.Activity;
import android.widget.EditText;

/**
 * Created by Administrator on 2017/2/12.
 */

public class ExitRomImpl    implements ExitRoom {
    @Override
    public boolean exit(Activity    activity) {
        boolean flag=false;
       VidyoSampleApplicationkevin app;
        app = (VidyoSampleApplicationkevin) activity.getApplication();
        app.DisableAllVideoStreams();
        app.Dispose();
        app.uninitialize();
        flag=true;
        activity.finish();


        return flag;
    }
}
