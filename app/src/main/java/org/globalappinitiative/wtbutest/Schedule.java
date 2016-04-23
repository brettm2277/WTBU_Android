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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Calendar;
import java.util.Locale;

public class Schedule extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, AdapterView.OnItemSelectedListener {

    private ImageView buttonPlay;       //play button
    private ImageView buttonPause;      //pause button

    private TextView textView_artist_name; // artist name at bottom
    private TextView textView_song_name;   // song name at bottom

    private AudioManager audioManager;  //allows for changing the volume

    private Spinner spinner;

    private ListView[] lists;

    private ArrayList<ArrayList<ScheduleItem>> schedule;

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

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ///////////////////////////////////////////////////////////

        initializeUI();

        ((MyApplication) this.getApplication()).updateContext(Schedule.this);
        getSchedule();
        for (int i = 0; i < 7; i++) {
            lists[i].setAdapter(new customListAdapter(this, schedule.get(i)));
        }
    }

    protected void initializeUI() {
        schedule = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            ArrayList<ScheduleItem> innerList = new ArrayList<ScheduleItem>();
            schedule.add(innerList);
        }

        lists = new ListView[7];
        lists[0] = (ListView) findViewById(R.id.day_list_monday);
        lists[1] = (ListView) findViewById(R.id.day_list_tuesday);
        lists[2] = (ListView) findViewById(R.id.day_list_wednesday);
        lists[3] = (ListView) findViewById(R.id.day_list_thursday);
        lists[4] = (ListView) findViewById(R.id.day_list_friday);
        lists[5] = (ListView) findViewById(R.id.day_list_saturday);
        lists[6] = (ListView) findViewById(R.id.day_list_sunday);

        buttonPlay = (ImageView) findViewById(R.id.buttonPlay);                                             //initializes play button
        buttonPlay.setOnClickListener(this);                                                                //sets click listener for the play button

        buttonPause = (ImageView) findViewById(R.id.buttonPause);
        buttonPause.setOnClickListener(this);

        if (((MyApplication) this.getApplication()).isPlaying()) {
            buttonPlay.setVisibility(View.INVISIBLE);
            buttonPause.setVisibility(View.VISIBLE);
        }

        else {
            buttonPlay.setVisibility(View.VISIBLE);
            buttonPause.setVisibility(View.INVISIBLE);
        }

        textView_artist_name = (TextView) findViewById(R.id.textView_artist_name);
        textView_song_name = (TextView) findViewById(R.id.textView_song_name);
        textView_artist_name.setText(((MyApplication) getApplication()).getArtistName());
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
        // TODO: setCurrentProgramRed(hour);
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
                                    String weekday = weekdays.getString(j);
                                    // Now finally construct the ScheduleItem from the parsed data
                                    ScheduleItem program = new ScheduleItem(weekday, showTime, showName);
                                    schedule.get(program.getDayOfWeek()).add(program);
                                }
                            }
                            // Sort the shows for each day
                            for (int i = 0; i < 7; i++) {
                                Collections.sort(schedule.get(i));
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
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
        this.position = position;
        // Hide all the scrollViews
        for (int i = 0; i < 7; i++) {
            lists[i].setVisibility(View.INVISIBLE);
        }
        // Show the one that corresponds to the position (which for some reason does not begin at zero)
        lists[position].setVisibility(View.VISIBLE);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }


    private class customListAdapter extends BaseAdapter {

        private List<ScheduleItem> listElements;
        private LayoutInflater inflater;

        public customListAdapter(Context context, ArrayList<ScheduleItem> singleDaySchedule) {
            this.listElements = singleDaySchedule;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return listElements.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return listElements.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.schedule_entry, parent, false);
            }

            ScheduleItem show = (ScheduleItem) getItem(position);
            TextView hour = (TextView) convertView.findViewById(R.id.schedule_entry_hour);
            TextView name = (TextView) convertView.findViewById(R.id.schedule_entry_text);
            hour.setText(Integer.toString(show.getShowTime()));
            name.setText(show.getTitle());

            return convertView;
        }
    }
}
