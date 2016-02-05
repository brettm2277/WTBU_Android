package org.globalappinitiative.wtbutest;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.parse.Parse;

import java.io.IOException;

/**
 * Created by BrettM on 11/10/2015.
 */
public class MyApplication extends Application {

    //Media player start/stop now controlled in this class. Can call start/stop from any class to control it
    private MediaPlayer player;

    private Context context;

    private Intent resultIntent;
    private Intent recieverIntent;

    private PendingIntent resultPendingIntent;
    private PendingIntent pendingIntentCancel;
    private NotificationCompat.Builder mBuilder;

    private NotificationManager manager;

    public void onCreate() {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "1YQrSQX8ISBBkVdXA2tgSmv0j2cBOx878Es5d5lD", "zplY28RZTzs5SqiUog33vcDlCIqP7FaJcVS28daA");

        initializeMediaPlayer();
    }

    public void updateContext(Context context) {    //call this when any new activity is started
        this.context = context;
    }

    public void initializeMediaPlayer() {
        player = new MediaPlayer();
        try {
            player.setDataSource("http://wtbu.bu.edu:1800/listen");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error", e.toString());
        }

    }

    public void startPlaying() {
        //prepare media player
        if (!player.isPlaying()) {
            try {
                player.prepareAsync();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            //show progress dialog while it's loading
            final ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Loading Stream...");
            progressDialog.show();

            //change notification to show the loading icon
            mBuilder.mActions.clear();  //remove the play icon
            mBuilder.addAction(R.drawable.ic_loading_white_24dp, "", pendingIntentCancel).build();  //change icon to the loading icon
            manager.notify(2, mBuilder.build());    //show notification

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    //once it's loaded, close progress dialog and start player
                    progressDialog.hide();
                    player.start();
                    //change notification action to stop button
                    mBuilder.mActions.clear();  //remove loading icon
                    mBuilder.addAction(R.drawable.ic_stop_white_24dp, "", pendingIntentCancel).build();     //add stop button to notification
                    manager.notify(2, mBuilder.build());    //show notification
                }
            });
        }
    }

    public void stopPlaying() {
        if (player.isPlaying())
        {
            player.stop();
            player.release();
            initializeMediaPlayer();
            //change notification action to play button
            mBuilder.mActions.clear();
            mBuilder.addAction(R.drawable.ic_play_arrow_white_24dp, "", pendingIntentCancel).build();
            manager.notify(2, mBuilder.build());
        }
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void createNotification(String artist, String title, Bitmap art) {   //does the initial creation of the notification
        resultIntent = new Intent(context, MainActivity.class);             //starts MainActivity
        recieverIntent = new Intent(context, NotificationReceiver.class);   //starts NotificationReceiver which detects when the action to play/stop is clicked

        //pending intents used for the notification
        resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        pendingIntentCancel = PendingIntent.getBroadcast(context, 0, recieverIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        mBuilder = new NotificationCompat.Builder(this);    //build notification
        mBuilder.setSmallIcon(R.drawable.ic_play_arrow_white_24dp)
                .setLargeIcon(art)
                .setContentTitle("WTBU")
                .setContentText(artist)
                .setSubText(title)
                .addAction(isPlaying()  ? R.drawable.ic_stop_white_24dp
                                        : R.drawable.ic_play_arrow_white_24dp
                                        , "", pendingIntentCancel)
                .setStyle(new NotificationCompat.MediaStyle().setShowCancelButton(true))    //MediaStyle makes it look nice
                .setContentIntent(resultPendingIntent)                                      //when the main part of notification is clicked
                .build();
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(2, mBuilder.build());                                                //show notification
    }

    public void updateNotificationInfo(String artist, String title, Bitmap art) {           //updates the notification with new album art and artist/song
        mBuilder.setLargeIcon(art)
                .setContentText(artist)
                .setSubText(title)
                .build();
        manager.notify(2, mBuilder.build());
    }
}
