package org.globalappinitiative.wtbutest;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by BrettM on 3/19/2016.
 */

//service that starts itself when the app is killed, and then removes the main notification with the album art
//since the play/stop button doesn't work when the app is killed
public class KillNotificationsService extends Service {

    public class KillBinder extends Binder {
        public final Service service;

        public KillBinder(Service service) {
            this.service = service;
        }

    }

    public static int NOTIFICATION_ID = 666;
    private NotificationManager mNM;
    private final IBinder mBinder = new KillBinder(this);

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNM.cancel(NOTIFICATION_ID);
    }
}
