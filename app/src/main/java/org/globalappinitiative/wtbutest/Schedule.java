package org.globalappinitiative.wtbutest;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Calendar;
import java.util.Locale;

public class Schedule extends AppCompatActivity {

    private ImageView buttonPlay;       //play button
    private ImageView buttonPause;      //pause button

    private TextView textView_artist_name; // artist name at bottom
    private TextView textView_song_name;   // song name at bottom

    private AudioManager audioManager;  //allows for changing the volume

    private Document htmlDocument;
    private String htmlPageUrl = "http://www.wtburadio.org/programming/";

    private Spinner spinner;

    private ScrollView[] scrollViews;
    private LinearLayout[] linearLayouts;

    private List<ScheduleItem>[] schedule;

    private boolean first_time = true;

    private boolean first_time_favorite = true;

    RequestQueue queue;

    private int first_time_favorite_index;

    private boolean first_time_unfavorite = true;

    private int first_time_unfavorite_index;

    private int position;

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

        ///////////////////////////////////////////////////////////

        initializeUI();

        ((MyApplication) this.getApplication()).updateContext(Schedule.this);
        getSchedule();
    }

    protected void initializeUI() {

        scrollViews = new ScrollView[7];
        scrollViews[0] = (ScrollView) findViewById(R.id.SundaySchedule);
        scrollViews[1] = (ScrollView) findViewById(R.id.MondaySchedule);
        scrollViews[1].setVisibility(View.INVISIBLE);
        scrollViews[2] = (ScrollView) findViewById(R.id.TuesdaySchedule);
        scrollViews[2].setVisibility(View.INVISIBLE);
        scrollViews[3] = (ScrollView) findViewById(R.id.WednesdaySchedule);
        scrollViews[3].setVisibility(View.INVISIBLE);
        scrollViews[4] = (ScrollView) findViewById(R.id.ThursdaySchedule);
        scrollViews[4].setVisibility(View.INVISIBLE);
        scrollViews[5] = (ScrollView) findViewById(R.id.FridaySchedule);
        scrollViews[5].setVisibility(View.INVISIBLE);
        scrollViews[6] = (ScrollView) findViewById(R.id.SaturdaySchedule);
        scrollViews[6].setVisibility(View.INVISIBLE);

        linearLayouts = new LinearLayout[7];
        linearLayouts[0] = (LinearLayout) findViewById(R.id.SundaysChildLayout);
        linearLayouts[1] = (LinearLayout) findViewById(R.id.MondaysChildLayout);
        linearLayouts[2] = (LinearLayout) findViewById(R.id.TuesdaysChildLayout);
        linearLayouts[3] = (LinearLayout) findViewById(R.id.WednesdaysChildLayout);
        linearLayouts[4] = (LinearLayout) findViewById(R.id.ThursdaysChildLayout);
        linearLayouts[5] = (LinearLayout) findViewById(R.id.FridaysChildLayout);
        linearLayouts[6] = (LinearLayout) findViewById(R.id.SaturdaysChildLayout);

        //animate_vertical(); <-- Call this later after knowing what day it is and populating the schedule...

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

        textView_artist_name = (TextView) findViewById(R.id.textView_artist_name);
        textView_song_name = (TextView) findViewById(R.id.textView_song_name);
        textView_artist_name.setText(((MyApplication)getApplication()).getArtistName());
        textView_song_name.setText(((MyApplication) getApplication()).getSongName());
        textView_song_name.setMovementMethod(new ScrollingMovementMethod()); // Allows this to scroll if song name too long

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);                              //AudioManager allows for changing of volume
        queue = Volley.newRequestQueue(this);
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
        //Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        //Use custom menu instead
        MenuInflater inflater = getMenuInflater();
        //Inflate the custom menu
        inflater.inflate(R.menu.schedule_menu, menu);
        //reference to the item of the menu
        MenuItem mitem = menu.findItem(R.id.item1);
        spinner = (Spinner) mitem.getActionView();
        setupSpinner(spinner);
        return true;
    }

    public void animate_vertical() {
        /*for (int i=0; i<10; i++)
        {
            TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 100, 0);   //from x, to x, from y, to y
            translateAnimation.setDuration(500);
            translateAnimation.setStartOffset(75 * i);
            translateAnimation.setInterpolator(new DecelerateInterpolator());
            relativeLayouts[i].startAnimation(translateAnimation);
        }*/
    }

    public void animate_fade() {
        /*for (int i=0; i<10; i++)
        {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0.2f);   //animate from visible to transparent
            alphaAnimation.setDuration(250);
            alphaAnimation.setStartOffset(10 * i);
            alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            alphaAnimation.setFillAfter(true);

            relativeLayouts[i].startAnimation(alphaAnimation);
        }*/
    }

    public void setupSpinner(Spinner spinner) {
        String[] items = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        //wrap the items in the Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Schedule.this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(R.layout.spinner_text);

        //assign adapter to the Spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_WEEK);  //sunday = 1, saturday = 7
        int hour = c.get(Calendar.HOUR_OF_DAY);
        setCurrentProgramRed(hour);
        Log.d("Hour", Integer.toString(hour));
        Log.d("Day", Integer.toString(day));
        spinner.setSelection(day - 1);

    }

    private void getSchedule() {
        String url = "https://gaiwtbubackend.herokuapp.com/regularShowsInfo?SongID=1234";

        queue.add(new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Get the results from the response JSON
                            JSONArray resultsJSON = response.getJSONArray("results");
                            // Now iterate through the results and add them to the programming list
                            for (int i = 0; i < resultsJSON.length(); i++) {
                                JSONObject result = resultsJSON.getJSONObject(i);
                                // Get the show name from the result json
                                String showName = result.getString("ShowName");
                                // Get the show time as well. Stored in the JSON in the form: hr:mim:sec, just need to convert the hours (first two characters) to an integer
                                int showTime = Integer.parseInt(result.getString("OnairTime").substring(0, 2)); // <--- Why is the end index exclusive? Is this a standard Java thing?
                                // If the showtime is at midnight (0 hours), set it to 24 for the purposes of sorting
                                // The same show can occur multiple times per week, so be sure to add all of them. Begin by getting the JSON array of weekdays
                                JSONArray weekdays = result.getJSONArray("Weekdays");
                                // Now for each entry in weekdays:
                                for (int j = 0; j < weekdays.length(); j++) {
                                    // Get the weekday out of the array
                                    String weekday = weekdays.getString(j);
                                    // Now finally construct the ScheduleItem from the parsed data
                                    ScheduleItem program = new ScheduleItem(weekday, showTime, showName);
                                    ///// TODO schedule[program.getDayOfWeek()][showTime] = program;
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

    public void setCurrentProgramRed(int hour) {
        /*if (hour < 6) {
            hour = 24;
        }
        else if (hour % 2 != 0) {
            hour = hour - 1;
        }
        Log.d("New hour", Integer.toString(hour));
        linearLayouts[hour/2-3].setBackgroundResource(R.drawable.red_square); // at 6 set 0th entry, at 8 set 1st entry, etc.*/
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

        } else if (id == R.id.nav_donate) {
            Intent intent = new Intent(this, DonateActivity.class);
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
        /*
        for (int i=0; i<10; i++) {
            if (schedule[position][i] != null)
            if (view == starButtons[i]) {
                if (((MyApplication) this.getApplication()).checkFavorite(position, i)) {
                    if (first_time_unfavorite) {
                        first_time_unfavorite_index = i;
                        first_time_unfavorite = false;
                        AlertDialog.Builder builder = new AlertDialog.Builder(Schedule.this, 0x01030228);   //Theme_Material_Dialog_NoActionBar
                        Log.d("this Position", Integer.toString(position));
                        builder.setMessage("No longer get notified when " + schedule[position][i].getTitle() + " is on?")
                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ((MyApplication) Schedule.this.getApplication()).removeFavorite(position, first_time_unfavorite_index);
                                        starButtons[first_time_unfavorite_index].setBackgroundResource(R.drawable.star_empty);
                                    }
                                })
                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        // Create the AlertDialog object and return it
                        builder.create();
                        builder.show();

                    }
                    else {
                        ((MyApplication) this.getApplication()).removeFavorite(position, i);
                        starButtons[i].setBackgroundResource(R.drawable.star_empty);
                    }
                }

                else {
                    if (first_time_favorite) {
                        first_time_favorite_index = i;
                        first_time_favorite = false;
                        AlertDialog.Builder builder = new AlertDialog.Builder(Schedule.this, 0x01030228);   //Theme_Material_Dialog_NoActionBar
                        Log.d("this Position", Integer.toString(position));
                        builder.setMessage("Get notified when " + schedule[position][i].getTitle() + " is on?")
                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        ((MyApplication) Schedule.this.getApplication()).addFavorite(position, first_time_favorite_index);
                                        starButtons[first_time_favorite_index].setBackgroundResource(R.drawable.star_full);
                                    }
                                })
                                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                        // Create the AlertDialog object and return it
                        builder.create();
                        builder.show();
                    }
                    else {
                        ((MyApplication) this.getApplication()).addFavorite(position, i);
                        starButtons[i].setBackgroundResource(R.drawable.star_full);
                    }
                }
            }
        }*/


        }


}
