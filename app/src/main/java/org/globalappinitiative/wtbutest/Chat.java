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
import android.widget.Spinner;
import android.widget.TextView;

import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.List;

public class Chat extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{


    private EditText editTextName;
    private EditText editTextRecipient;
    private EditText editTextMessage;

    private Button buttonSignIn;
    private Button buttonSendMessage;
    private String name;
    private String recipient;
    private ScrollView pastMessages;
    private String message;
    private LinearLayout textBar;

    private static final String APP_KEY = "e6fd4b52-4e46-4335-8f39-b0045235ace7";
    private static final String APP_SECRET = "XnBsdW4jh0+lrvRdOuzH2A==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";
    private SinchClient sinchClient = null;
    private MessageClient messageClient = null;


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
        editTextRecipient = (EditText) findViewById(R.id.editTextRecipient);
        editTextRecipient.setVisibility(View.INVISIBLE);
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

    public void setupSinch(String userName) {
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this.getApplicationContext())
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost("sandbox.sinch.com")
                .userId(userName)
                .build();
        sinchClient.setSupportMessaging(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.checkManifest();
        sinchClient.addSinchClientListener(new SinchClientListener() {
            @Override
            public void onClientStarted(SinchClient sinchClient) {
                sinchClient.startListeningOnActiveConnection();
                Log.d("Sinch", "started");
            }

            @Override
            public void onClientStopped(SinchClient sinchClient) {
                Log.d("Sinch", "stopped");
            }

            @Override
            public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {
                Log.d("Sinch", "failed");
            }

            @Override
            public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {
                Log.d("Sinch", "credentials required");
            }

            @Override
            public void onLogMessage(int i, String s, String s1) {

            }
        });

        sinchClient.start();
        messageClient = sinchClient.getMessageClient();
        messageClient.addMessageClientListener(new MessageClientListener() {
            @Override
            public void onIncomingMessage(MessageClient messageClient, Message message) {
                Log.d("Sinch", "Message received");
                Log.d("Message", message.getTextBody());

                // Now store the message within the scroll view's linear layout child
                LinearLayout mLayout = (LinearLayout) findViewById(R.id.childLayout);
                TextView tv = new TextView(getApplicationContext());
                // Put the user's message into the text view
                tv.setText(message.getTextBody());
                // Set the font color of the text view
                tv.setTextColor(Color.parseColor("#FFFFFF"));
                // Add a background image (the nine patch text bubble, the other one is called red bubble but it's actually gray)
                tv.setBackgroundResource(R.drawable.actually_red_bubble);
                // Set the size for the textview
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                TextView blank = new TextView(getApplicationContext());
                blank.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                TextView blank2 = new TextView(getApplicationContext());
                blank2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                // Add the text view to the layout
                mLayout.addView(blank);
                mLayout.addView(tv);
                mLayout.addView(blank2);

                // Now scroll to the bottom of the scrollview so that the new message shows up
                pastMessages.post(new Runnable() {
                    @Override
                    public void run() {
                        pastMessages.fullScroll(ScrollView.FOCUS_DOWN);
                    }

                });
            }

            @Override
            public void onMessageSent(MessageClient messageClient, Message message, String s) {
                Log.d("Sinch", "Message sent");
            }

            @Override
            public void onMessageFailed(MessageClient messageClient, Message message, MessageFailureInfo messageFailureInfo) {
                Log.d("Sinch", "Failed to send to user: " + messageFailureInfo.getRecipientId()
                        + " because: " + messageFailureInfo.getSinchError().getMessage());
            }

            @Override
            public void onMessageDelivered(MessageClient messageClient, MessageDeliveryInfo messageDeliveryInfo) {
                Log.d("Sinch", "The message with id " + messageDeliveryInfo.getMessageId()
                        + " was delivered to the recipient with id " + messageDeliveryInfo.getRecipientId());
            }

            @Override
            public void onShouldSendPushData(MessageClient messageClient, Message message, List<PushPair> list) {

            }
        });

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
            editTextRecipient.setVisibility(View.VISIBLE);
            name = editTextName.getText().toString();
            editTextName.setVisibility(View.INVISIBLE);
            pastMessages.setVisibility(View.VISIBLE);
            editTextMessage.setVisibility(View.VISIBLE);
            buttonSendMessage.setVisibility(View.VISIBLE);
            setupSinch(name);
            buttonSignIn.setVisibility(View.INVISIBLE);
            // Make the text bar visible
            textBar.setVisibility(View.VISIBLE);
        }
        if (view == buttonSendMessage) {
            editTextMessage.setVisibility(View.VISIBLE);
            recipient = editTextRecipient.getText().toString();
            message = editTextMessage.getText().toString();

            // Only attempt to send the message if it contains something and the user has selected a recipient
            if (message != "" && recipient != "") {
                WritableMessage writableMessage = new WritableMessage(recipient, message);
                Log.d("Sinch", "sending message");
                messageClient.send(writableMessage);

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
