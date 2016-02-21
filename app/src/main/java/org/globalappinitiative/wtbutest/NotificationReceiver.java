package org.globalappinitiative.wtbutest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by BrettM on 12/4/2015.
 */
public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Notification", "clicked");
        Log.d("Context", context.toString());

        if (((MyApplication) context.getApplicationContext()).isPlaying()) {
            ((MyApplication) context.getApplicationContext()).stopPlaying();
        }
        else {
            Log.d("Context passed", context.toString());
                    ((MyApplication) context.getApplicationContext()).startPlaying();
        }

    }
}
