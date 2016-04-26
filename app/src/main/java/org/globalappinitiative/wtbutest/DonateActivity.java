package org.globalappinitiative.wtbutest;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

public class DonateActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private ImageView buttonPlay;       //play button
    private ImageView buttonPause;      //pause button

    //private AudioManager audioManager;  //allows for changing the volume

    public TextView textView_artist_name;
    public TextView textView_song_name;

    private WebView donate_page;

    //onCreate runs when app first starts//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_donate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
        initializeUI();
        ((MyApplication) this.getApplication()).updateContext(DonateActivity.this);

    }

    // NEEDS TO BE PRIVATE (WHO KNOWS WHY?)
    private void initializeUI() {

        donate_page = (WebView) findViewById(R.id.webview); // load webview
        donate_page.loadUrl("https://trusted.bu.edu/s/1759/2-bu/giving.aspx?sid=1759&gid=2&pgid=434&cid=1077&appealcode=WEBCOM");
        WebSettings webSettings = donate_page.getSettings();
        webSettings.setJavaScriptEnabled(true);

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
        textView_artist_name.setText(((MyApplication)getApplication()).getArtistName());
        textView_song_name.setText(((MyApplication) getApplication()).getSongName());
        textView_song_name.setMovementMethod(new ScrollingMovementMethod()); // Allows this to scroll if song name too long
        // Instantiate the RequestQueue.
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

    //created by navigation drawer template. we can change it later for whatever we put in it
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_playing) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_schedule) {
            Intent intent = new Intent(this, Schedule.class);
            startActivity(intent);

        } else if (id == R.id.nav_donate) {
            // do nothing
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