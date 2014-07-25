
package com.example.autostart;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

public class BootTestService extends Service {

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
//            Log.d("BootTestService", "BootTestService test");
            sendEmptyMessageDelayed(0, 2000);
        };
    };

    @Override
    public void onCreate() {
        super.onCreate();
        handler.sendEmptyMessage(0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
