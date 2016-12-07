package org.globalappinitiative.wtbu;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

/**
 * Created by BrettM on 3/19/2016.
 */

//runs when the alarm goes off to notify user that show is on now
//creates the actual notification
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, SplashActivity.class), 0);  //start app from splash screen when notification clicked

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);    //build notification
        mBuilder.setSmallIcon(R.drawable.ic_play_arrow_white_24dp)
                .setContentTitle("WTBU")
                .setContentText("A show you favorited is starting!")
                //.setSubText("Subtext")
                .setContentIntent(pendingIntent)        //when the main part of notification is clicked
                .setAutoCancel(true)        //remove notification when clicked
                .build();
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, mBuilder.build());
    }
}
