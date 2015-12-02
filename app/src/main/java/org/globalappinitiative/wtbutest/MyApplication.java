package org.globalappinitiative.wtbutest;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.parse.Parse;

import java.io.IOException;

/**
 * Created by BrettM on 11/10/2015.
 */
public class MyApplication extends Application {

    //Media player start/stop now controlled in this class. Can call start/stop from any class to control it
    private MediaPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "1YQrSQX8ISBBkVdXA2tgSmv0j2cBOx878Es5d5lD", "zplY28RZTzs5SqiUog33vcDlCIqP7FaJcVS28daA");

        initializeMediaPlayer();

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

    public void startPlaying(Context context) {
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

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    //once it's loaded, close progress dialog and start player
                    progressDialog.hide();
                    player.start();
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
        }
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }
}
