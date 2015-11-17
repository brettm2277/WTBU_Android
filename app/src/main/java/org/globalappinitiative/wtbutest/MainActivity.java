package org.globalappinitiative.wtbutest;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private ImageView buttonPlay;       //play button
    private ImageView buttonPause;      //pause button
    private SeekBar volumeBar;          //volume bar

    private AudioManager audioManager;  //allows for changing the volume

    private TextView textViewtest;

    //onCreate runs when app first starts//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);                         //set the interface to the xml file activity_main
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);         //initialize the toolbar at the top
        setSupportActionBar(toolbar);                                   //allows the toolbar to have the capabilities of an action bar

        setVolumeControlStream(AudioManager.STREAM_MUSIC);              //makes it so when user uses volume keys it raises the music volume, not ringer volume

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
    }

    private void initializeUI() {
        buttonPlay = (ImageView) findViewById(R.id.buttonPlay);                                             //initializes play button
        buttonPlay.setOnClickListener(this);                                                                //sets click listener for the play button

        buttonPause = (ImageView) findViewById(R.id.buttonPause);
        buttonPause.setOnClickListener(this);

        //check whether or not the audio is currently playing
        if (((MyApplication) this.getApplication()).isPlaying()) {                  //If audio already playing, show pause button
            buttonPlay.setVisibility(View.INVISIBLE);
            buttonPause.setVisibility(View.VISIBLE);
        } else {                                                                    //If audio not playing yet, show play button
            buttonPlay.setVisibility(View.VISIBLE);
            buttonPause.setVisibility(View.INVISIBLE);
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);                              //AudioManager allows for changing of volume
        int current_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeBar = (SeekBar) findViewById(R.id.volumeBar);                                                 //initializes seekbar which acts as the volume slider
        volumeBar.setProgress(current_volume * 7);
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {                        //seekBarChangeListener runs whenever the volume slider is moved
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {                              //returns an integer i which tells us out of 100 how far the slider is moved to the right
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i / 6, AudioManager.FLAG_SHOW_UI);    //the volume is out of 15, so doing i/6 allows for an even distribution of volume across the slider
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        textViewtest = (TextView) findViewById(R.id.textView_test);
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://spinitron.com/radio/rss.php?station=wtbu";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        List<Song> songLog = new ArrayList<>();
                        Parser parser = new Parser();
                        songLog = parser.parse(response, songLog);
                        String s = "";
                        for (int i = 0; i < songLog.size(); i++) {
                            s = s + songLog.get(i).getArtist() + "\n";
                        }
                        textViewtest.setText(s);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textViewtest.setText("That didn't work!");
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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

        if (id == R.id.nav_playing) {

        } else if (id == R.id.nav_schedule) {
            Intent intent = new Intent(this, Schedule.class);
            startActivity(intent);

        } else if (id == R.id.nav_history) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_chat) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view == buttonPlay) {
            ((MyApplication) this.getApplication()).startPlaying(this);
            buttonPlay.setVisibility(View.INVISIBLE);
            buttonPause.setVisibility(View.VISIBLE);
        }
        if (view == buttonPause) {
            ((MyApplication) this.getApplication()).stopPlaying();
            buttonPlay.setVisibility(View.VISIBLE);
            buttonPause.setVisibility(View.INVISIBLE);
        }
    }

/*    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super();
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) || keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            int current_volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            volumeBar.setProgress(current_volume * 7);
        }
        return true;
    }
*/
}