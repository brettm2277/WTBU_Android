package org.globalappinitiative.wtbutest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
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

class XMLParser { // Changed from class Parser due to import issues
    public List<Song> parse(String inputXML, List<Song> songLog) {
        Document songList = Jsoup.parse(inputXML, "", Parser.xmlParser());
        boolean hasSeenTitle = false;	// The RSS feed xml has one instance of '<title>' before the ones preceding actual song info
        if (songLog.isEmpty()) {		// If the song log is completely empty, we need to get all of the song history
										/* NOTE: parsing the XML from top to bottom reads the songs into the list in reverse order, so
										 we'll need to reverse the list at the end (only the first time though, after that just add new
										 songs to the end of the list */
            Elements songItems = songList.select("item");
            for (Element song : songItems) {
                Element title = song.select("title").first();
                String artistAndTitle = title.text();
                String[] split = artistAndTitle.split(": ");
                String artistName = split[0];
                String songTitle = split[1].substring(1, split[1].length() - 1);
                Song tempSong = new Song(songTitle, artistName); //  Declare a song, assign the parsed values to it, and add it to the songLog list
                try { // If publish date available, provide start time as well
                    Element pubDate = song.select("pubDate").first();
                    String timeStart = pubDate.text();
                    SimpleDateFormat f = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");
                    Date d = f.parse(timeStart);
                    long songStart = d.getTime();
                    tempSong.setStart(songStart);		// Set the start of the song
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (songLog.isEmpty() || !tempSong.isSameSong(songLog.get(songLog.size() - 1))) {
                    songLog.add(tempSong);
                }
            }
            // Reading from beginning to end puts elements into the arraylist in reverse order, so flip it
            Collections.reverse(songLog);
        }
        return songLog;
    }
}

