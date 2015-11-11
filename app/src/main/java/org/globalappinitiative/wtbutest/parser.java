package org.globalappinitiative.wtbutest;

/**
 * Created by evanbowman on 11/10/15.
 */
import java.util.*;

class Song {
    private String songTitle;			// A string to store the song title
    private String artistName;			// A string to store the artist name

    public String getTitle() {			// An accessor function that returns the song title
        return songTitle;
    }

    public String getArtist() {			// An accessor function that returns the artist name
        return artistName;
    }

    public void setTitle(String title) {	// A function to set the song title
        songTitle = title;
    }

    public void setArtist(String artist) {	// A function to set the artist name
        artistName = artist;
    }
}

class Parser {
    public List<Song> parse(String inputXML, List<Song> songLog) {
        boolean hasSeenTitle = false;	// The RSS feed xml has one instance of '<title>' before the ones preceding actual song info
        if (songLog.isEmpty()) {		// If the song log is completely empty, we need to get all of the song history
										/* NOTE: parsing the XML from top to bottom reads the songs into the list in reverse order, so
										 we'll need to reverse the list at the end (only the first time though, after that just add new
										 songs to the end fo the list */
            for (int i = 0; i < inputXML.length() - 6; ) {
                String myString = "<title>";
                if (inputXML.substring(i, i + 7).equals("<title>") && hasSeenTitle) {	// If the parser sees '<title>'
                    i += 7;				// Why check the condition over again later when we've already seen '<title>'? So jump ahead!
                    String artistName = "";	// A temporary variable to hold the artist name
                    while (inputXML.charAt(i) != ':') {
                        char c = inputXML.charAt(i);
                        String myChar = Character.toString(c);	// Convert the char to a string in order to concatenate with artist name
                        artistName += myChar;
                        i++;
                    }
                    i += 3;				// Skip the semicolon, space, and apostraphe following the artist name
                    String songTitle = "";	// A temporary variable to hold the song title
                    while (inputXML.charAt(i + 1) != '<') {	// If we have not reached the single-quote that terminates the song title
                        // Note: compare to the the symbol after the apostraphe in case the song
                        //title includes single-quotes
                        char c = inputXML.charAt(i);
                        String myChar = Character.toString(c);
                        songTitle += myChar;
                        i++;
                    }
                    i += 110;						/* Parts of the xml are variable, but due to xml format there are also
													 keywords of fixed length that we can safely skip over (for example,
													we don't need to run the loop for <link> and <pubdate> */
                    Song tempSong = new Song();		// Declare a song, assign the parsed values to it, and add it to the songLog list
                    tempSong.setArtist(artistName);
                    tempSong.setTitle(songTitle);
                    songLog.add(tempSong);
                }
                else if (inputXML.substring(i, i + 7).equals("<title>") && !hasSeenTitle) {
                    String testStr = inputXML.substring(i, i + 7);
                    hasSeenTitle = true;
                    i++;
                }
                else {
                    i++;
                }
            }
            // Reading from beginning to end puts elements into the arraylist in reverse order, so flip it
            Collections.reverse(songLog);
        }

		/*If there's something in the song log, there's no reason to re-add everything. Just add the most
		 recent song (as long as it has a different title and artist.*/
        else if (!(songLog.isEmpty())) {
            for (int i = 0; i < inputXML.length() - 6; ) {
                String myString = "<title>";
                if (inputXML.substring(i, i + 7).equals("<title>") && hasSeenTitle) {
                    i += 7;
                    String artistName = "";
                    while (inputXML.charAt(i) != ':') {
                        char c = inputXML.charAt(i);
                        String myChar = Character.toString(c);
                        artistName += myChar;
                        i++;
                    }
                    i += 3;
                    String songTitle = "";
                    while (inputXML.charAt(i + 1) != '<') {
                        char c = inputXML.charAt(i);
                        String myChar = Character.toString(c);
                        songTitle += myChar;
                        i++;
                    }
                    Song tempSong = new Song();
                    tempSong.setArtist(artistName);
                    tempSong.setTitle(songTitle);
                    // Now only add a new song if both the artist and title are different
                    if (tempSong.getArtist() != songLog.get(songLog.size() - 1).getArtist() && tempSong.getTitle() != songLog.get(songLog.size() - 1).getTitle()) {
                        songLog.add(tempSong);
                    }
                    // If we've got the first song, no reason to loop through the rest of the string, so exit
                    return songLog;
                }
                else if (inputXML.substring(i, i + 7).equals("<title>") && !hasSeenTitle) {
                    String testStr = inputXML.substring(i, i + 7);
                    hasSeenTitle = true;
                    i++;
                }
                else {
                    i++;
                }
            }
        }
        return songLog;
    }
}

