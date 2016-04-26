package org.globalappinitiative.wtbutest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Chat extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private static final String baseURL = "https://gaiwtbubackend.herokuapp.com/";
    private static final String chatSessionURL = baseURL + "chatSession";
    private static final String getMessagesURL = baseURL + "getMessages";
    private static final String sendMessageURL = baseURL + "sendChatMessage";

    private EditText editTextName;
    private EditText editTextRecipient;
    private EditText editTextMessage;

    private Button buttonSignIn;
    private Button buttonSendMessage;
    private String name;
    private String token;
    private String recipient;
    private ScrollView pastMessages;
    private String message;
    private LinearLayout textBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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
        ((MyApplication) this.getApplication()).updateContext(Chat.this);
    }

    public void initializeUI() {
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);

        pastMessages = (ScrollView) findViewById(R.id.pastMessages);
        pastMessages.setVisibility(View.INVISIBLE);
        buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
        buttonSendMessage = (Button) findViewById(R.id.buttonSendMessage);
        buttonSignIn.setOnClickListener(this);
        buttonSendMessage.setOnClickListener(this);
        editTextMessage.setVisibility(View.INVISIBLE);
        textBar = (LinearLayout) findViewById(R.id.linearLayout);
        // Start off with the text bar for messaging invisible
        textBar.setVisibility(View.INVISIBLE);
        //spinnerRecipient.setVisibility(View.INVISIBLE);
    }

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
            Intent intent = new Intent(this, DonateActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_chat) {
            // do nothing
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view == buttonSignIn) {
            final String requestedName  = editTextName.getText().toString();
            Map<String, String> map = new HashMap<String, String>();
            map.put("name", requestedName);
            AppVolleyState.instance().getRequestQueue().add(new JSONGETRequest(chatSessionURL, map,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("JSON Object", response.toString());
                            try {
                                name = response.getString("name");
                                token = response.getString("token");

                                editTextRecipient.setVisibility(View.VISIBLE);
                                editTextName.setVisibility(View.INVISIBLE);
                                pastMessages.setVisibility(View.VISIBLE);
                                editTextMessage.setVisibility(View.VISIBLE);
                                buttonSendMessage.setVisibility(View.VISIBLE);
                                buttonSignIn.setVisibility(View.INVISIBLE);
                                // Make the text bar visible
                                textBar.setVisibility(View.VISIBLE);
                            } catch (JSONException e) {
                                Log.d("JSON Exception", e.getMessage());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Volley Error", error.getMessage());
                        }
                    }));
            Log.d("Chat Session Request", "Chat session request sent to server.");
        }
        if (view == buttonSendMessage) {
            editTextMessage.setVisibility(View.VISIBLE);
            message = editTextMessage.getText().toString();
            // Only attempt to send the message if it contains something
            if (message != "") {
                Map<String, String> map = new HashMap<String, String>();
                map.put("token", token);
                map.put("message", message);
                AppVolleyState.squickRequest(sendMessageURL, map,
                        new AppVolleyState.SquickRequestDelegate() {
                            @Override
                            public void onResponse(JSONObject response) {
                               // Do nothing for now, later check if successful.
                                Log.d("Volley Response", response.toString());
                            }
                        });

                // Get the id of the linear layout to place text into
                LinearLayout mLayout = (LinearLayout) findViewById(R.id.childLayout);
                TextView tv = new TextView(this);
                // Put the user's message into the text view
                tv.setText(message);
                // Set the font color of the text view
                tv.setTextColor(Color.parseColor("#FFFFFF"));
                // Add a background image (the nine patch text bubble)
                tv.setBackgroundResource(R.drawable.red_bubble);
                // Set the size for the textview
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                TextView blank = new TextView(this);
                blank.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                TextView blank2 = new TextView(this);
                blank2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                // Add the text view to the layout
                mLayout.addView(blank);
                mLayout.addView(tv);
                mLayout.addView(blank2);
                // Clear out the text from the text entry box
                editTextMessage.setText("");

                // Now scroll to the bottom of the scrollview so that the new message shows up
                pastMessages.post(new Runnable() {
                    @Override
                    public void run() {
                        pastMessages.fullScroll(ScrollView.FOCUS_DOWN);
                    }

                });
            }
        }
    }
}
