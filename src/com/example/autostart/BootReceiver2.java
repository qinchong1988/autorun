package com.example.autostart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver2 extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction().toString();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // u can start your service here
            Log.d("BootReceiver", "boot2 completed action");
            //Toast.makeText(context, "boot2 completed action has got", Toast.LENGTH_LONG).show();
            return;
        }
    }

}
