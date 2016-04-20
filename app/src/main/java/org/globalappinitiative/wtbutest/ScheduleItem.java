package org.globalappinitiative.wtbutest;

/**
 * Created by Edward on 2/22/2016.
 */
public class ScheduleItem implements Comparable<ScheduleItem> {

    private int dayOfWeek;   // Day of the week converted to string
    private int showTime; // Time of the show
    private String title;    // Title of the show

    public ScheduleItem(String weekday, int showTime, String title) {
        switch(weekday) {
            case "Sun":
                dayOfWeek = 0;
                break;
            case "Mon":
                dayOfWeek = 1;
                break;
            case "Tue":
                dayOfWeek = 2;
                break;
            case "Wed":
                dayOfWeek = 3;
                break;
            case "Thu":
                dayOfWeek = 4;
                break;
            case "Fri":
                dayOfWeek = 5;
                break;
            case "Sat":
                dayOfWeek = 6;
                break;
            default:
                dayOfWeek = -1;
        }
        this.showTime = showTime;
        this.title = title;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public String getTitle() {
        return title;
    }

    public int getShowTime() {
        return showTime;
    }

    @Override
    public int compareTo(ScheduleItem other) {
        return this.getShowTime() - other.getShowTime();
    }

    @Override
    public int hashCode() {
        return showTime;
    }
}
