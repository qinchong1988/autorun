
package com.example.autostart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PackageInstallReceiver extends BroadcastReceiver {

    private static final String TAG = PackageInstallReceiver.class.getSimpleName();
    private String[] mDisableQiyiBootReceiver = new String[] {
            "pm disable com.qiyi.video/org.qiyi.android.commonphonepad.pushmessage.PushMsgBroadCastReceiver",
            "pm disable com.qiyi.video/com.baidu.android.pushservice.PushServiceReceiver",
            "pm disable com.qiyi.video/tv.pps.bi.receiver.BIReceiver",
            "pm disable com.qiyi.video/com.baidu.android.moplus.MoPlusReceiver",
            "pm disable com.qiyi.video/com.iqiyi.sdk.android.pushservice.PushServiceReceiver"
    };
    
    private static final ExecutorService  mSingleThreadPool = Executors.newSingleThreadExecutor();

    @Override
    public void onReceive(Context context, Intent intent) {
        
        Log.d(TAG, "---------onReceive------intent = " + intent);

        Uri uri = intent.getData();
        Log.d(TAG, "get uri toString = " + uri.toString());
        Log.d(TAG, "get uri getAuthority = " + uri.getAuthority());
        Log.d(TAG, "get uri getPath = " + uri.getPath());
        Log.d(TAG, "get uri getHost = " + uri.getHost());
        Log.d(TAG, "get uri getScheme = " + uri.getScheme());
        Log.d(TAG, "get uri getEncodedSchemeSpecificPart = " + uri.getEncodedSchemeSpecificPart());
        Log.d(TAG, "get uri getSchemeSpecificPart = " + uri.getSchemeSpecificPart());

        String pkg = uri.getSchemeSpecificPart();
        if ("com.qiyi.video".equals(pkg)) {
            mSingleThreadPool.submit(new Runnable() {

                @Override
                public void run() {
                    for (String cmd : mDisableQiyiBootReceiver) {
                        MainActivity.execCmd(cmd);
                    }
                }
            });
        }
    }

}
