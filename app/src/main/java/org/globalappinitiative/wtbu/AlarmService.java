package org.globalappinitiative.wtbu;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by BrettM on 3/19/2016.
 */

//helper class for MyService that actually handles setting up when to notify the user
public class AlarmService {
    private Context context;
    private ArrayList<PendingIntent> intentArray;

    public AlarmService(Context context) {
        this.context = context;
        intentArray = new ArrayList<>();
    }

    public void startAlarm() {
        SharedPreferences favorites = context.getSharedPreferences("FavoritesFile", 0);     //load the list of which shows are favorites
        int request_code = 0;   //each alarm needs a different request code to be separate, so this gets incremented for each possible show
        Calendar now = Calendar.getInstance();  //get current time

        for (int day = 0; day < 7; day++) {
            for (int time = 0; time < 24; time++) {
                request_code++;
                boolean fav = favorites.getBoolean(Integer.toString(day) + " " + Integer.toString(time), false);    //get if show is a favorite

                Calendar alarm = Calendar.getInstance();
                alarm.setTimeInMillis(System.currentTimeMillis());
                //int t;  //need time and day values that work properly with the Android Calendar class
                int d = day + 1;    //for Android Calendar class, Sunday = 1, Saturday = 7

                alarm.set(Calendar.DAY_OF_WEEK, d);     //+1 since sunday = 1 and saturday = 7 in calendar
                alarm.set(Calendar.HOUR_OF_DAY, time);     //set hour of show
                alarm.set(Calendar.MINUTE, 0);          //shows are always at the start of the hour

                long alarm_millis;
                if (alarm.getTimeInMillis() < now.getTimeInMillis()) {      //if the favorite show has happened already this week
                    alarm_millis = alarm.getTimeInMillis() + (AlarmManager.INTERVAL_DAY * 7);   //set to notify next week (Android instantly launches all alarms that are in the past)
                    //Log.d("Day and Time is Before", Integer.toString(d) + " " + Integer.toString(time));
                } else {        //if the favorite show has yet to happen this week
                    alarm_millis = alarm.getTimeInMillis();
                    //Log.d("Day and Time", Integer.toString(d) + " " + Integer.toString(time));
                }

                if (fav) {
                    PendingIntent mAlarmSender = PendingIntent.getBroadcast(context, request_code, new Intent(context, AlarmReceiver.class), 0);    //AlarmReceiver class will run when alarm goes off which creates the notification
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarm_millis, AlarmManager.INTERVAL_DAY * 7, mAlarmSender);   //set to repeat alarm each week
                    intentArray.add(mAlarmSender);
                    //Log.d("Day and Time Set", Integer.toString(d) + " " + Integer.toString(time));
                } else {
                    PendingIntent mAlarmSender = PendingIntent.getBroadcast(context, request_code, new Intent(context, AlarmReceiver.class), 0);    //AlarmReceiver class will run when alarm goes off which creates the notification
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.cancel(mAlarmSender);
                    intentArray.add(mAlarmSender);
                    //Log.d("Day and Time Canceled", Integer.toString(d) + " " + Integer.toString(time));
                }

            }
        }
    }
}
