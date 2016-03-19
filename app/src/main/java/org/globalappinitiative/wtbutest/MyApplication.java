package org.globalappinitiative.wtbutest;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
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

    private String artist_name;
    private String song_name;

    private PendingIntent resultPendingIntent;
    private PendingIntent pendingIntentCancel;
    private NotificationCompat.Builder mBuilder;

    private NotificationManager manager;

    ProgressDialog progressDialog;

    private boolean first_time = true;
    private boolean ready_to_play = false;
    private boolean clicked_play = false;

    private boolean [][] usrFavorites = new boolean[7][10];


    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "1YQrSQX8ISBBkVdXA2tgSmv0j2cBOx878Es5d5lD", "zplY28RZTzs5SqiUog33vcDlCIqP7FaJcVS28daA");

        initializeMediaPlayer();
        preparePlayer();
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

    public void preparePlayer() {       //gets player ready in the background
        if (!player.isPlaying()) {
            try {
                player.prepareAsync();  //prepare
            } catch (IllegalStateException e) { //catch exceptions
                e.printStackTrace();
            }
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {       //runs when player is ready
                if (!clicked_play) {        //if the play button wasn't clicked, but we're done pre-loading in background
                    ready_to_play = true;   //ready to play
                }
                else {  //if the play button was clicked and the user is waiting
                    progressDialog.hide();  //hide progress dialog
                    player.start();         //start player
                    //change notification action to stop button
                    mBuilder.mActions.clear();  //remove loading icon
                    mBuilder.addAction(R.drawable.ic_stop_white_24dp, "", pendingIntentCancel).build();     //add stop button to notification
                    manager.notify(KillNotificationsService.NOTIFICATION_ID, mBuilder.build());    //show notification
                }
            }
        });
    }

    public void startPlaying() {
        if (first_time) {   //if it's the first time the app is running
            if (ready_to_play) {    //if the player is done loading and is ready
                player.start();     //start the player
                clicked_play = true;    //set clicked to true, probably not useful since won't be used again
                //change notification action to stop button
                mBuilder.mActions.clear();  //remove loading icon
                mBuilder.addAction(R.drawable.ic_stop_white_24dp, "", pendingIntentCancel).build();     //add stop button to notification
                manager.notify(KillNotificationsService.NOTIFICATION_ID, mBuilder.build());    //show notification
            } else {                //if it's not done loading yet
                clicked_play = true;    //the user clicked play
                //show progress dialog while it's loading
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Loading Stream...");
                progressDialog.show();

                //change notification to show the loading icon
                mBuilder.mActions.clear();  //remove the play icon
                mBuilder.setSmallIcon(R.drawable.ic_play_arrow_white_24dp); // set small icon to notify that radio is playing
                mBuilder.addAction(R.drawable.ic_loading_white_24dp, "", pendingIntentCancel).build();  //change icon to the loading icon
                manager.notify(KillNotificationsService.NOTIFICATION_ID, mBuilder.build());    //show notification
            }
            first_time = false; //no longer the first time
        }
        else {  //if it's not the first time, load the player normally and show progress dialog

            //prepare media player
            if (!player.isPlaying()) {
                try {
                    player.prepareAsync();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }

                //show progress dialog while it's loading
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("Loading Stream...");
                progressDialog.show();

                //change notification to show the loading icon
                mBuilder.mActions.clear();  //remove the play icon
                mBuilder.addAction(R.drawable.ic_loading_white_24dp, "", pendingIntentCancel).build();  //change icon to the loading icon
                manager.notify(KillNotificationsService.NOTIFICATION_ID, mBuilder.build());    //show notification

                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        //once it's loaded, close progress dialog and start player
                        progressDialog.hide();
                        player.start();
                        //change notification action to stop button
                        mBuilder.mActions.clear();  //remove loading icon
                        mBuilder.addAction(R.drawable.ic_stop_white_24dp, "", pendingIntentCancel).build();     //add stop button to notification
                        manager.notify(KillNotificationsService.NOTIFICATION_ID, mBuilder.build());    //show notification
                    }
                });
            }
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
            mBuilder.setSmallIcon(R.drawable.ic_stop_white_24dp); // set small icon to notify that player is stopped
            mBuilder.addAction(R.drawable.ic_play_arrow_white_24dp, "", pendingIntentCancel).build();
            manager.notify(KillNotificationsService.NOTIFICATION_ID, mBuilder.build());
        }
    }

    public void setArtistName(String name) {
        Log.d("Artist Name: ", name);
        artist_name = name;
    }

    public void setSongName(String name) {
        Log.d("Song Name: ", name);
        song_name = name;
    }

    public String getArtistName() {
        return artist_name;
    }

    public String getSongName() {
        return song_name;
    }

    // Add an accessor method for setting the volume of the media player
    public void setVolume(int value) {
        // setVolume takes a fraction between 0 and 1, and input is out of 100, so need to convert
        float convertedSliderVal = ((float) value) / 100;
        // setVolume takes a left value and a right value, I believe that this pans the sound between speakers sort of like stereo sound. Can anyone confirm? --Evan
        player.setVolume(convertedSliderVal, convertedSliderVal);
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void createNotification(String artist, String title, Bitmap art) {   //does the initial creation of the notification
        resultIntent = new Intent(context, MainActivity.class);             //starts MainActivity
        recieverIntent = new Intent(context, NotificationReceiver.class);   //starts NotificationReceiver which detects when the action to play/stop is clicked

    public void createNotification(final String artist, final String title, final Bitmap art) {   //does the initial creation of the notification

        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                ((KillNotificationsService.KillBinder) binder).service.startService(new Intent(
                        context, KillNotificationsService.class));
                resultIntent = new Intent(context, MainActivity.class);             //starts MainActivity
                recieverIntent = new Intent(context, NotificationReceiver.class);   //starts NotificationReceiver which detects when the action to play/stop is clicked

                //pending intents used for the notification
                resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                pendingIntentCancel = PendingIntent.getBroadcast(context, 0, recieverIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Log.d("Service", "builder");
                mBuilder = new NotificationCompat.Builder(context);    //build notification
                mBuilder.setSmallIcon(R.drawable.ic_stop_white_24dp)
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
                manager.notify(KillNotificationsService.NOTIFICATION_ID, mBuilder.build());                                                //show notification

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        bindService(new Intent(context, KillNotificationsService.class), serviceConnection, Context.BIND_AUTO_CREATE);

    }

    public void updateNotificationInfo(String artist, String title, Bitmap art) {           //updates the notification with new album art and artist/song
        Log.d("Update", "notification");
        mBuilder.setLargeIcon(art)
                .setContentText(artist)
                .setSubText(title)
                .build();
        manager.notify(KillNotificationsService.NOTIFICATION_ID, mBuilder.build());
    }

    public void setup_prefs() {
        SharedPreferences favorites = getSharedPreferences("FavoritesFile", 0);
        SharedPreferences.Editor editor = favorites.edit();
        for (int day=0; day<7; day++)
        {
            for (int time = 0; time < 10; time++)
            {
                editor.putBoolean(Integer.toString(day) + " " + Integer.toString(time), false); // ex: 2 4 means Tuesday 2PM since the 2PM show is the fourth one
            }
        }
        // Commit the edits!
        editor.apply();
    }


    public void add_favorite(int day, int time) {
        SharedPreferences favorites = getSharedPreferences("FavoritesFile", 0);
        SharedPreferences.Editor editor = favorites.edit();

        editor.putBoolean(Integer.toString(day) + " " + Integer.toString(time), true);
        editor.apply();
    }

    public void remove_favorite(int day, int time) {
        SharedPreferences favorites = getSharedPreferences("FavoritesFile", 0);
        SharedPreferences.Editor editor = favorites.edit();

        editor.putBoolean(Integer.toString(day) + " " + Integer.toString(time), false);
        editor.apply();
    }

    public boolean check_favorite(int day, int time) {
        return usrFavorites[day][time];
    }

    public void removeNotification() {
        manager.cancelAll();
    }

}
