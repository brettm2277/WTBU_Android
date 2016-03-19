package org.globalappinitiative.wtbutest;

import android.content.Context;
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

import android.util.Log;
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
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private ImageView buttonPlay;       //play button
    private ImageView buttonPause;      //pause button

    private AudioManager audioManager;  //allows for changing the volume

    public TextView textView_artist_name;
    public TextView textView_song_name;

    private ImageView album_art;

    RequestQueue queue;                 //used with volley, holds all of the requests (rss feed, album art)

    Handler handler = new Handler();    //used with the auto refresh runnable

    Bitmap art;

    Song nowPlaying;

    String current_artist;
    String current_title;
    String artist_and_title;

    //onCreate runs when app first starts//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);                         //set the interface to the xml file activity_main
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);         //initialize the toolbar at the top
        setSupportActionBar(toolbar);                                   //allows the toolbar to have the capabilities of an action bar
        nowPlaying = new Song("", "");
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

        getRSSData();

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

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);                              //AudioManager allows for changing of volume

        textView_artist_name = (TextView) findViewById(R.id.textView_artist_name);
        textView_song_name = (TextView) findViewById(R.id.textView_song_name);
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this);

    }

    private void getRSSData()
    {
        String url = "https://spinitron.com/radio/rss.php?station=wtbu";        //rss feed gives us list of recently played songs. need something more up to date

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,    //use volley to make request to get rss feed data
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        List<Song> songLog = new ArrayList<Song>();
                        XMLParser parser = new XMLParser();
                        songLog = parser.parse(response, songLog);
                        String s = "";
                        for (int i = songLog.size()-1; i >= 0; i--) {
                            s = s + songLog.get(i).getArtist().replace("&amp;", "&") + "\n";
                        }
                        if (!songLog.get(songLog.size()-1).isSameSong(nowPlaying)) { // no need to get album information if song is the same
                            nowPlaying = songLog.get(songLog.size() - 1);
                            current_artist = nowPlaying.getArtist().replace("&amp;", "&").replace("(", "").replace(")", "");    //get most recent artist
                            current_title = nowPlaying.getTitle().replace("&amp;", "&").replace("(", "").replace(")", "");      //get most recent song
                            ((MyApplication) MainActivity.this.getApplication()).setArtistName(current_artist);
                            ((MyApplication) MainActivity.this.getApplication()).setSongName(current_title);

                            artist_and_title = current_artist + " - " + current_title;

                            textView_artist_name.setText(current_artist);
                            textView_song_name.setText(current_title);

                            getSongArtLength(nowPlaying);                                            //get the url for the album artwork for this song
                        }
                        //((MyApplication) MainActivity.this.getApplication()).updateNotificationInfo(current_artist, current_title, art);    //update notification with new album art, song/title

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", error.toString());
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);       //add the request to the queue
    }

    private void getSongArtLength(final Song song)        //uses the free iTunes api to get album artwork url
    {
        String artist_and_title = song.getArtist() + " " + song.getTitle();
        String url = "https://itunes.apple.com/search?term=" + artist_and_title.replaceAll(" ", "+");   //add artist and title to url
        Log.d("URL", url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,        //use volley to make a string request
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);           //iTunes api gives data back in json format, this parses it
                            JSONArray results = jsonObject.getJSONArray("results");
                            JSONObject res = results.getJSONObject(0);
                            String artwork_url = res.getString("artworkUrl100");
                            int index = artwork_url.indexOf("100x100bb.jpg");
                            artwork_url = artwork_url.substring(0, index) + "1200x1200bb.jpg";        //artwork_url contains image of album artwork
                            Log.d("Artwork URL", artwork_url);
                            getAlbumArt(artwork_url);                                 //need to make another request using volley to actually get the image
                            int song_length = res.getInt("trackTimeMillis");          // gets length of song from API
                            song.setTrackLength(song_length);                         // set the length of the song
                        } catch (JSONException e) {
                            Log.e("JSON error", e.toString());
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VolleyError", error.toString());
            }
        });
        queue.add(stringRequest);       //add the request to the queue
    }

    private void getAlbumArt(String url)    //make request using volley to actually download the album art
    {
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
        queue.add(imageRequest);
    }

    public Runnable runnable = new Runnable() {         //runs every 30 seconds, refreshes song/artist and album art
        @Override
        public void run() {
            if (nowPlaying.getSongEnd() < Calendar.getInstance().getTimeInMillis() ) {
                album_art.setImageResource(R.drawable.cover_art_android);
            }
            getRSSData();   //gets RSS data, which calls the getAlbumArtURL function, which calls the getAlbumArt function, refreshing the song/artist and album art
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

        } else if (id == R.id.nav_chat) {
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

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            if (((MyApplication) this.getApplication()).isPlaying()) {
                ((MyApplication) this.getApplication()).stopPlaying();
                buttonPlay.setVisibility(View.VISIBLE);
                buttonPause.setVisibility(View.INVISIBLE);
            }
            else {
                ((MyApplication) this.getApplication()).startPlaying();
                buttonPlay.setVisibility(View.INVISIBLE);
                buttonPause.setVisibility(View.VISIBLE);
            }
        }
        return true;
    }*/
}