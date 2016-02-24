package org.globalappinitiative.wtbutest;

/**
 * Created by Edward on 2/22/2016.
 */
public class ScheduleItem {

    private int dayOfWeek;   // Day of the week converted to string
    private String url;      // URL of the string
    private String showTime; // Time of the show
    private String title;    // Title of the show

    public ScheduleItem(String weekday, String url, String showTime, String title) {
        switch(weekday.toLowerCase()) {
            case "sunday":
                dayOfWeek=0;
                break;
            case "monday":
                dayOfWeek = 1;
                break;
            case "tuesday":
                dayOfWeek = 2;
                break;
            case "wednesday":
                dayOfWeek = 3;
                break;
            case "thursday":
                dayOfWeek = 4;
                break;
            case "friday":
                dayOfWeek = 5;
                break;
            case "saturday":
                dayOfWeek = 6;
                break;
            default:
                dayOfWeek = -1;
        }
        this.url = url;
        this.showTime = showTime;
        this.title = title;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public String getTitle() {
        return title;
    }
}
