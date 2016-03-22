package org.globalappinitiative.wtbutest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by BrettM on 3/19/2016.
 */

//service that handles notifying when your favorite show is on
public class MyService extends Service {

    AlarmService alarmService;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //start tasks
        Log.d("Notification Service", "started");
        alarmService = new AlarmService(getApplicationContext());
        alarmService.startAlarm();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //stop tasks
        Log.d("Notification Service", "ended");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}