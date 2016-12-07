package org.globalappinitiative.wtbu;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by BrettM on 3/19/2016.
 */

//service that handles notifying when your favorite show is on
public class MyService extends Service {

    AlarmService alarmService;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //start tasks
        alarmService = new AlarmService(getApplicationContext());
        alarmService.startAlarm();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //stop tasks
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}