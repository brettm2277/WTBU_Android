package org.globalappinitiative.wtbutest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private ImageView buttonPlay;       //play button
    private ImageView buttonPause;      //pause button

    //private AudioManager audioManager;  //allows for changing the volume

    public TextView textView_artist_name;
    public TextView textView_song_name;

    private ImageView album_art;

    Handler handler = new Handler();    //used with the auto refresh runnable

    String currentSongName = "";

    Bitmap art;

    long songEnd = 0;

    String current_artist;
    String current_title = "";
    String artist_and_title;

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

        ((MyApplication) this.getApplication()).updateContext(MainActivity.this);

        ((MyApplication) this.getApplication()).createNotification(current_artist, current_title, art); //create notification

        getSongInfo();

        handler.postDelayed(runnable, 30000);   //Runnable will run after 30000 milliseconds, or 30 seconds
    }



    // NEEDS TO BE PRIVATE (WHO KNOWS WHY?)
    private void initializeUI() {
        album_art = (ImageView) findViewById(R.id.album_art);

        buttonPlay = (ImageView) findViewById(R.id.buttonPlay);                                             //initializes play button
        buttonPlay.setOnClickListener(this);                                                                //sets click listener for the play button

        buttonPause = (ImageView) findViewById(R.id.buttonPause);
        buttonPause.setOnClickListener(this);

        //check whether or not the audio is currently playing
        if (((MyApplication) this.getApplication()).isPlaying()) {                  //If audio already playing, show pause button
            buttonPlay.setVisibility(View.INVISIBLE);
            buttonPause.setVisibility(View.VISIBLE);
        }
        else {                                                                    //If audio not playing yet, show play button
            buttonPlay.setVisibility(View.VISIBLE);
            buttonPause.setVisibility(View.INVISIBLE);
        }

        //audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);                              //AudioManager allows for changing of volume

        textView_artist_name = (TextView) findViewById(R.id.textView_artist_name);
        textView_song_name = (TextView) findViewById(R.id.textView_song_name);
        textView_song_name.setMovementMethod(new ScrollingMovementMethod()); // Allows this to scroll if song name too long
        // Instantiate the RequestQueue.

    }

    private void getSongInfo() {
        String url = "https://gaiwtbubackend.herokuapp.com/song?SongID=1234";        // SPINITRON is down at the time of writing this, so change to https://gaiwtbubackend.herokuapp.com/song when it's back online

        AppVolleyState.instance().getRequestQueue().add(new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Only do the rest of the parsing if the JSON does not contain errors
                            if (!response.has("errors")) {
                                // Get the results from the response JSON
                                JSONObject resultsJSON = response.getJSONObject("results");
                                // Now set the current artist and song title based on the results JSON
                                String songTitle = resultsJSON.getString("SongName");
                                if (!songTitle.equals(current_title)) {
                                    current_artist = resultsJSON.getString("ArtistName");
                                    current_title = songTitle;
                                    // Set the artist and song name in the app
                                    ((MyApplication) MainActivity.this.getApplication()).setArtistName(current_artist);
                                    ((MyApplication) MainActivity.this.getApplication()).setSongName(current_title);
                                    textView_artist_name.setText(current_artist);
                                    textView_song_name.setText(current_title);
                                    // Get the album art JSON
                                    JSONObject albumArtJSON = resultsJSON.getJSONObject("AlbumArt");
                                    String artUrl = albumArtJSON.getString("1000x1000");
                                    // Get the album art with another volley request
                                    setAlbumArt(artUrl);
                                    // Parse the start time out of the JSON
                                    String date = resultsJSON.getString("Date");
                                    String time = resultsJSON.getString("Timestamp");
                                    Calendar c = Calendar.getInstance();
                                    c.set(Integer.parseInt(date.substring(0, 3)), Integer.parseInt(date.substring(5, 6)), Integer.parseInt(date.substring(8, 9)),
                                            Integer.parseInt(time.substring(0, 1)), Integer.parseInt(time.substring(3, 4)), Integer.parseInt(date.substring(6, 7)));
                                    songEnd = c.getTimeInMillis() + Integer.parseInt(resultsJSON.getString("TrackTimeMillis"));
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", error.toString());
                    }
                }));
    }

    private void setAlbumArt(String url) {
        //url is the url for the album art given by the iTunes api
        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {        //says ImageRequest is deprecated although it still works. May need a different solution
            @Override
            public void onResponse(Bitmap response) {
                art = response;
                album_art.setImageBitmap(response);         //set image with album art if it worked
                ((MyApplication) MainActivity.this.getApplication()).updateNotificationInfo(current_artist, current_title, art);    //update notification with new album art, song/title
            }
        }, 0, 0, null,
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
        AppVolleyState.instance().getRequestQueue().add(imageRequest);
    }

    public Runnable runnable = new Runnable() {         //runs every 30 seconds, refreshes song/artist and album art
        @Override
        public void run() {
            if (songEnd < Calendar.getInstance().getTimeInMillis() ) {
                album_art.setImageResource(R.drawable.cover_art_android);
            }
            getSongInfo();   //gets RSS data, which calls the getAlbumArtURL function, which calls the setAlbumArt function, refreshing the song/artist and album art
            handler.postDelayed(this, 30000);   //will run again in 30 seconds
        }
    };

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
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 30000);   //Runnable will run after 30000 milliseconds, or 30 seconds
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

        } else if (id == R.id.nav_donate) {
            Intent intent = new Intent(this, DonateActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_chat) {
            Intent intent = new Intent(this, Chat.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view == buttonPlay) {
            ((MyApplication) this.getApplication()).startPlaying();
            buttonPlay.setVisibility(View.INVISIBLE);
            buttonPause.setVisibility(View.VISIBLE);
        }
        if (view == buttonPause) {
            ((MyApplication) this.getApplication()).stopPlaying();
            buttonPlay.setVisibility(View.VISIBLE);
            buttonPause.setVisibility(View.INVISIBLE);
        }
    }
}