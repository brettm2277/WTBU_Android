package org.globalappinitiative.wtbutest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.util.Log;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    private Document htmlDocument;
    private String htmlPageUrl = "http://www.wtburadio.org/programming/";

    private Spinner spinner;

    ArrayList<ArrayList<ScheduleItem>> allPrograms = new ArrayList<ArrayList<ScheduleItem>>();

    private TextView[] textView_Programs = new TextView[10];
    private View[] starButtons = new View[10];
    private LinearLayout[] linearLayouts = new LinearLayout[10];
    private RelativeLayout[] relativeLayouts = new RelativeLayout[10];

    private boolean first_time = true;

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
        for (int i = 0; i < 7; i++) {
            allPrograms.add(new ArrayList<ScheduleItem>()); // Add seven ArrayLists
        }
        initializeUI();
        ((MyApplication) this.getApplication()).updateContext(Schedule.this);
        new JsoupAsyncTask().execute();

    }

    protected void initializeUI()
    {
        textView_Programs[0] = (TextView) findViewById(R.id.textView_program1);
        textView_Programs[1] = (TextView) findViewById(R.id.textView_program2);
        textView_Programs[2] = (TextView) findViewById(R.id.textView_program3);
        textView_Programs[3] = (TextView) findViewById(R.id.textView_program4);
        textView_Programs[4] = (TextView) findViewById(R.id.textView_program5);
        textView_Programs[5] = (TextView) findViewById(R.id.textView_program6);
        textView_Programs[6] = (TextView) findViewById(R.id.textView_program7);
        textView_Programs[7] = (TextView) findViewById(R.id.textView_program8);
        textView_Programs[8] = (TextView) findViewById(R.id.textView_program9);
        textView_Programs[9] = (TextView) findViewById(R.id.textView_program10);

        relativeLayouts[0] = (RelativeLayout) findViewById(R.id.rl1);
        relativeLayouts[1] = (RelativeLayout) findViewById(R.id.rl2);
        relativeLayouts[2] = (RelativeLayout) findViewById(R.id.rl3);
        relativeLayouts[3] = (RelativeLayout) findViewById(R.id.rl4);
        relativeLayouts[4] = (RelativeLayout) findViewById(R.id.rl5);
        relativeLayouts[5] = (RelativeLayout) findViewById(R.id.rl6);
        relativeLayouts[6] = (RelativeLayout) findViewById(R.id.rl7);
        relativeLayouts[7] = (RelativeLayout) findViewById(R.id.rl8);
        relativeLayouts[8] = (RelativeLayout) findViewById(R.id.rl9);
        relativeLayouts[9] = (RelativeLayout) findViewById(R.id.rl10);

        linearLayouts[0] = (LinearLayout) findViewById(R.id.block1);
        linearLayouts[1] = (LinearLayout) findViewById(R.id.block2);
        linearLayouts[2] = (LinearLayout) findViewById(R.id.block3);
        linearLayouts[3] = (LinearLayout) findViewById(R.id.block4);
        linearLayouts[4] = (LinearLayout) findViewById(R.id.block5);
        linearLayouts[5] = (LinearLayout) findViewById(R.id.block6);
        linearLayouts[6] = (LinearLayout) findViewById(R.id.block7);
        linearLayouts[7] = (LinearLayout) findViewById(R.id.block8);
        linearLayouts[8] = (LinearLayout) findViewById(R.id.block9);
        linearLayouts[9] = (LinearLayout) findViewById(R.id.block10);

        starButtons[0] = (View) findViewById(R.id.star1);
        starButtons[1] = (View) findViewById(R.id.star2);
        starButtons[2] = (View) findViewById(R.id.star3);
        starButtons[3] = (View) findViewById(R.id.star4);
        starButtons[4] = (View) findViewById(R.id.star5);
        starButtons[5] = (View) findViewById(R.id.star6);
        starButtons[6] = (View) findViewById(R.id.star7);
        starButtons[7] = (View) findViewById(R.id.star8);
        starButtons[8] = (View) findViewById(R.id.star9);
        starButtons[9] = (View) findViewById(R.id.star10);

        for (int i=0; i<10; i++)
        {
            starButtons[i].setOnClickListener(this);
        }


        animate_vertical();

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

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);                              //AudioManager allows for changing of volume
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
        for (int i=0; i<9; i++)
        {
            TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 100, 0);   //from x, to x, from y, to y
            translateAnimation.setDuration(500);
            translateAnimation.setStartOffset(75 * i);
            translateAnimation.setInterpolator(new DecelerateInterpolator());
            relativeLayouts[i].startAnimation(translateAnimation);
        }
    }

    public void animate_fade() {
        for (int i=0; i<9; i++)
        {
            AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0.2f);   //animate from visible to transparent
            alphaAnimation.setDuration(250);
            alphaAnimation.setStartOffset(10 * i);
            alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            alphaAnimation.setFillAfter(true);

            relativeLayouts[i].startAnimation(alphaAnimation);
        }
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

    public void setCurrentProgramRed(int hour) {
        if (hour < 6) {
            hour = 24;
        }
        else if (hour % 2 != 0) {
            hour = hour - 1;
        }
        Log.d("New hour", Integer.toString(hour));
        linearLayouts[hour/2-3].setBackgroundResource(R.drawable.red_square); // at 6 set 0th entry, at 8 set 1st entry, etc.
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

        for (int i=0; i<10; i++) {
            if (view == starButtons[i]) {
                if (((MyApplication) this.getApplication()).check_favorite(position, i)) {
                    ((MyApplication) this.getApplication()).remove_favorite(position, i);
                    starButtons[i].setBackgroundResource(R.drawable.star_empty);
                }

                else {
                    ((MyApplication) this.getApplication()).add_favorite(position, i);
                    starButtons[i].setBackgroundResource(R.drawable.star_full);
                }

                /*AlertDialog.Builder builder = new AlertDialog.Builder(Schedule.this, 0x01030228);   //Theme_Material_Dialog_NoActionBar
                Log.d("this Position", Integer.toString(position));
                builder.setMessage("Get notified when " + allPrograms.get(position).get(i).getTitle() + " is on?")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                // Create the AlertDialog object and return it
                builder.create();
                builder.show();*/
            }
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
                            Pattern weekdayPattern = Pattern.compile("(sun|mon|tues|wednes|thurs|fri|satur)day");
                            Matcher matcher = weekdayPattern.matcher(relHref);
                            if (matcher.find()) {
                                String showDay = matcher.group(0); // Capitalize the first letter
                                String showName = cols.get(j).text();
                                ScheduleItem program = new ScheduleItem(showDay, relHref, showTime, showName);
                                allPrograms.get(program.getDayOfWeek()).add(program);
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
            Calendar c = Calendar.getInstance();
            int currentDay = c.get(Calendar.DAY_OF_WEEK) - 1;
            for (int i = 0; i < allPrograms.get(currentDay).size(); i++) {
                String title = "\n" + allPrograms.get(currentDay).get(i).getTitle() + "\n";
                textView_Programs[i].setText(title);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
        this.position = position;
        for (int i = 0; i < 10; i++) {
            if (((MyApplication) this.getApplication()).check_favorite(position, i)) {
                starButtons[i].setBackgroundResource(R.drawable.star_full);
            }

            else {
                starButtons[i].setBackgroundResource(R.drawable.star_empty);
            }
        }

        if (first_time) {
            first_time = false;
            Log.d("Position", Integer.toString(position));
            for (int i = 0; i < allPrograms.get(position).size(); i++) {
                String title = "\n" + allPrograms.get(position).get(i).getTitle() + "\n";
                textView_Programs[i].setText(title);
            }
        }
        else {
            animate_fade();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    animate_vertical();
                    Log.d("Position", Integer.toString(position));
                    for (int i = 0; i < allPrograms.get(position).size(); i++) {
                        String title = "\n" + allPrograms.get(position).get(i).getTitle() + "\n";
                        textView_Programs[i].setText(title);
                    }
                }
            }, 300);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
