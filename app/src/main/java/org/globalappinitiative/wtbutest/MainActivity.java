package org.globalappinitiative.wtbutest;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
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
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener,
        AlbumFragment.OnFragmentInteractionListener,
        ScheduleFragment.OnFragmentInteractionListener,
        DonateFragment.OnFragmentInteractionListener  {

    private ImageView buttonPlay;       //play button
    private ImageView buttonPause;      //pause button

    //private AudioManager audioManager;  //allows for changing the volume

    public TextView textView_artist_name;
    public TextView textView_song_name;

    public String artURL;

    Handler handler = new Handler();    //used with the auto refresh runnable

    FragmentManager fragmentManager = getFragmentManager();

    Bitmap art;

    long songEnd = 0;

    String current_artist;
    String current_title = "";

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

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment, new AlbumFragment(), "Album");
        fragmentTransaction.commit();

        getSongInfo();

        handler.postDelayed(runnable, 30000);   //Runnable will run after 30000 milliseconds, or 30 seconds
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
        }
        else {                                                                    //If audio not playing yet, show play button
            buttonPlay.setVisibility(View.VISIBLE);
            buttonPause.setVisibility(View.INVISIBLE);
        }

        textView_artist_name = (TextView) findViewById(R.id.textView_artist_name);
        textView_song_name = (TextView) findViewById(R.id.textView_song_name);
        textView_song_name.setMovementMethod(new ScrollingMovementMethod()); // Allows this to scroll if song name too long
        // Instantiate the RequestQueue.

    }

    private void getSongInfo() {
        String url = "https://gaiwtbubackend.herokuapp.com/song";

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
                                    artURL = albumArtJSON.getString("1000x1000");

                                    Log.d("Album: ",artURL);
                                    // if AlbumFragment is visible show it
                                    AlbumFragment f = (AlbumFragment)fragmentManager.findFragmentByTag("Album");

                                    if (f != null && f.isVisible()) {
                                        f.changeURL(artURL);
                                    } else {
                                        Log.d("s: ", "is null?");
                                    }

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public Runnable runnable = new Runnable() {         //runs every 30 seconds, refreshes song/artist and album art
        @Override
        public void run() {
            getSongInfo();   //gets RSS data, which calls the getAlbumArtURL function, refreshing the song/artist
            handler.postDelayed(this, 30000);   //will run again in 30 seconds
        }
    };

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

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        if (id == R.id.nav_playing) {
            if (artURL != null) fragment = AlbumFragment.newInstance(artURL); // attach artURL to this
            title = "Now Playing";
        } else if (id == R.id.nav_schedule) {
            fragment = new ScheduleFragment();
            title = "Schedule";
        } else if (id == R.id.nav_donate) {
            fragment = new DonateFragment();
            title = "Donate";
        }
        /*
        else if (id == R.id.nav_chat) {
            Intent intent = new Intent(this, Chat.class);
            startActivity(intent);
        }
        */
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (title.equals("Now Playing")) {
                fragmentTransaction.replace(R.id.main_fragment, fragment, "Album");
            } else {
                fragmentTransaction.replace(R.id.main_fragment, fragment, "");
            }
            fragmentTransaction.commit();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
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

    @Override
    public void onFragmentInteraction(Uri uri){
        //you can leave it empty
    }
}