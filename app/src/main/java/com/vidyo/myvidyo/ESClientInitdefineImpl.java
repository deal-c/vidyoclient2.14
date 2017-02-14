package com.vidyo.myvidyo;

import android.app.Activity;
import android.util.Log;

/**
 * Created by Administrator on 2017/2/11.
 */

public class ESClientInitdefineImpl implements ESClientInitdefine {

    @Override
    public boolean ESClientInitdefine(Activity activity, String crtpath) {
        ESClientInitialize  esClientInitialize=new ESClientInitializeImpl();


   return esClientInitialize.ESClientInitialize(activity,crtpath);
    }
}
