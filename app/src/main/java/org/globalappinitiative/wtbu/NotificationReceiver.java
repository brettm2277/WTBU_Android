package org.globalappinitiative.wtbu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by BrettM on 12/4/2015.
 */
public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (((MyApplication) context.getApplicationContext()).isPlaying()) {
            ((MyApplication) context.getApplicationContext()).stopPlaying();
        }
        else {
                    ((MyApplication) context.getApplicationContext()).startPlaying();
        }

    }
}
