package org.globalappinitiative.wtbutest;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private boolean playing = false;    //switches between true and false depending on whether or not the stream is currently playing

    private ImageView buttonPlay;       //play button
    private SeekBar volumeBar;          //volume bar

    private MediaPlayer player;         //handles the streaming
    private AudioManager audioManager;  //allows for changing the volume


    //onCreate runs when app first starts//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);                         //set the interface to the xml file activity_main
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);         //initialize the toolbar at the top
        setSupportActionBar(toolbar);                                   //allows the toolbar to have the capabilities of an action bar

        //////////////////navigation drawer stuff//////////////////
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ///////////////////////////////////////////////////////////

        initializeUI();                 //initializes the features of the buttons and volume slider
        initializeMediaPlayer();        //initializes the media player which handles the streaming
    }

    private void initializeUI()
    {
        buttonPlay = (ImageView) findViewById(R.id.buttonPlay);                                             //initializes play button
        buttonPlay.setOnClickListener(this);                                                                //sets click listener for the play button
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);                              //AudioManager allows for changing of volume
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);                                                 //initializes seekbar which acts as the volume slider
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {                        //seekBarChangeListener runs whenever the volume slider is moved
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {                              //returns an integer i which tells us out of 100 how far the slider is moved to the right
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i/6, AudioManager.FLAG_SHOW_UI);    //the volume is out of 15, so doing i/6 allows for an even distribution of volume across the slider
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initializeMediaPlayer() {
        player = new MediaPlayer();
        try {
            player.setDataSource("http://wtbu.bu.edu:1800/listen");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Error", e.toString());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e("Error", e.toString());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Log.e("Error", e.toString());
        }
    }

    private void startPlaying() {
        try {
            player.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                playing = true;
                player.start();
            }
        });
    }

    private void stopPlaying() {
        if (player.isPlaying())
        {
            player.stop();
            player.release();
            initializeMediaPlayer();
            playing = false;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //created by navigation drawer template. we can change it later for whatever we put in it
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camara) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view == buttonPlay && !playing) {
            startPlaying();
        }
        else if (view == buttonPlay && playing) {
            stopPlaying();
        }
    }
}
