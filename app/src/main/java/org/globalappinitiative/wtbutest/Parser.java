package org.globalappinitiative.wtbutest;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by evanbowman on 11/10/15.
 */

class Song {
    private String songTitle;			// A string to store the song title
    private String artistName;			// A string to store the artist name
    private int trackLength;            // An integer that stores track length in milliseconds
    private long start, end;

    public Song(String title, String artist) {
        this.songTitle = title;
        this.artistName = artist;
        this.trackLength = 180000; // default 3 minutes
        start = Calendar.getInstance().getTimeInMillis();
        end = start+trackLength;
    }

    public Song(String title, String artist, long startTime) {
        this.songTitle = title;
        this.artistName = artist;
        this.trackLength = 180000; // default 3 minutes
        this.start = startTime;
        end = start+trackLength;
    }

    public String getTitle() {			// An accessor function that returns the song title
        return songTitle;
    }

    public String getArtist() {			// An accessor function that returns the artist name
        return artistName;
    }

    public int getTrackLength() { return trackLength;    }

    public long getSongEnd() {
        return end;
    }

    public void setTitle(String title) {	// A function to set the song title
        songTitle = title;
    }

    public void setStart(long startTime) { // Gets the time the song starts
        start = startTime;
        end = start + trackLength;
    }

    public void setArtist(String artist) {	// A function to set the artist name
        artistName = artist;
    }

    public void setTrackLength(int length) { // Sets track length and resets start of song
        trackLength = length;
        end = start + length;
    }

    public boolean isSameSong(Song s) { // Returns true if songs have the same title and author, false otherwise
        return (songTitle.equals(s.getTitle()) && artistName.equals(s.getArtist()));
    }
}

class ChatMessage implements Comparable<ChatMessage> {
    private String sender;
    private String message;
    private long timestamp;

    public ChatMessage(String sender, String message, long timestamp) {
        this.sender = sender;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public int compareTo(ChatMessage other) {
        long ret = other.timestamp - timestamp;
        if (ret > 0) return 1;
        if (ret < 0) return -1;
        return 0;
    }
}

class BackendQuery { // Changed from class Parser due to import issues
    public static Song parseSong(JSONObject inputJSON) {
        final JSONObject obj;
        try {
            JSONObject resultsJSON = inputJSON.getJSONObject("results");
            final String name = resultsJSON.getString("ArtistName");
            final String title = resultsJSON.getString("SongName");
            Song currentSong = new Song(name, title);
            return currentSong;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}