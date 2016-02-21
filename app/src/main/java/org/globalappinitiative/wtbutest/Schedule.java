package org.globalappinitiative.wtbutest;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Schedule extends MainActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private ImageView buttonPlay;       //play button
    private ImageView buttonPause;      //pause button
    private SeekBar volumeBar;          //volume bar

    private AudioManager audioManager;  //allows for changing the volume

    private Document htmlDocument;
    private String htmlPageUrl = "http://www.wtburadio.org/programming/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_schedule);
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
        ((MyApplication) this.getApplication()).updateContext(Schedule.this);
        new JsoupAsyncTask().execute();
    }

    protected void initializeUI()
    {
        buttonPlay = (ImageView) findViewById(R.id.buttonPlay);                                             //initializes play button
        buttonPlay.setOnClickListener(this);                                                                //sets click listener for the play button

        buttonPause = (ImageView) findViewById(R.id.buttonPause);
        buttonPause.setOnClickListener(this);

        if (((MyApplication) this.getApplication()).isPlaying())
        {
            buttonPlay.setVisibility(View.INVISIBLE);
            buttonPause.setVisibility(View.VISIBLE);
        }
        else
        {
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
                ((MyApplication) getApplication()).setVolume(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
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
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_schedule) {
            //do nothing

        } else if (id == R.id.nav_history) {

        } else if (id == R.id.nav_share) {

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

    private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> { // Reads the schedule data

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                htmlDocument = Jsoup.connect(htmlPageUrl).get(); // Connect to WTBU
                Element table = htmlDocument.select("table").get(0); // select the schedule table
                Elements rows = table.select("tr");
                for (int i=1; i < rows.size(); i++) {
                    Element tableRow = rows.get(i);
                    Elements cols = tableRow.select("td");
                    String showTime = cols.get(0).text();
                    for (int j=1; j < cols.size(); j++) { // Iterate over the td elements
                        Element link = cols.get(j).select("a").first();
                        if (link != null) {
                            String relHref = link.attr("href");
                            Pattern weekdayPattern = Pattern.compile("(mon|tues|wednes|thurs|fri|satur|sun)day");
                            Matcher matcher = weekdayPattern.matcher(relHref);
                            if (matcher.find()) {
                                String showDay = matcher.group(0); // Capitalize the first letter
                                showDay = showDay.substring(0,1).toUpperCase() + showDay.substring(1);
                                String showName = cols.get(j).text();
                                Log.d("show name", showName + " on " + showDay + " at" + showTime);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Load string data into views here.
        }
    }
}
