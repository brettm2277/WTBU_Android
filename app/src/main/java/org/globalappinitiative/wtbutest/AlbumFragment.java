package org.globalappinitiative.wtbutest;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AlbumFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AlbumFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlbumFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String IMAGE_URL = "cover_art_android";

    // TODO: Rename and change types of parameters
    private String imageURL;

    private ImageView album_art;

    Handler handler = new Handler();    //used with the auto refresh runnable

    Bitmap art;

    long songEnd = 0;

    String current_artist;
    String current_title = "";

    private OnFragmentInteractionListener mListener;

    public AlbumFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param imageURL The URL of the image to be loaded
     * @return A new instance of fragment AlbumFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AlbumFragment newInstance(String imageURL) {
        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URL, imageURL);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageURL = getArguments().getString(IMAGE_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    // Called after onCreateView, sets up the album stuff
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        album_art = (ImageView) getView().findViewById(R.id.album_art); // Creates album art
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void getSongInfo() {
        String url = "https://gaiwtbubackend.herokuapp.com/song";

        AppVolleyState.instance().getRequestQueue().add(new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Only do the rest of the parsing if the JSON does not contain errors
                            if (!response.has("errors")) {
                                // Get the results from the response JSON
                                JSONObject resultsJSON = response.getJSONObject("results");
                                // Now set the current artist and song title based on the results JSON
                                String songTitle = resultsJSON.getString("SongName");
                                if (!songTitle.equals(current_title)) {
                                    current_title = songTitle;
                                    // Get the album art JSON
                                    JSONObject albumArtJSON = resultsJSON.getJSONObject("AlbumArt");
                                    String artUrl = albumArtJSON.getString("1000x1000");
                                    // Get the album art with another volley request
                                    setAlbumArt(artUrl);
                                    // Parse the start time out of the JSON
                                    String date = resultsJSON.getString("Date");
                                    String time = resultsJSON.getString("Timestamp");
                                    Calendar c = Calendar.getInstance();
                                    c.set(Integer.parseInt(date.substring(0, 3)), Integer.parseInt(date.substring(5, 6)), Integer.parseInt(date.substring(8, 9)),
                                            Integer.parseInt(time.substring(0, 1)), Integer.parseInt(time.substring(3, 4)), Integer.parseInt(date.substring(6, 7)));
                                    songEnd = c.getTimeInMillis() + Integer.parseInt(resultsJSON.getString("TrackTimeMillis"));
                                }
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

    private void setAlbumArt(String url) {
        //url is the url for the album art given by the iTunes api
        ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {        //says ImageRequest is deprecated although it still works. May need a different solution
            @Override
            public void onResponse(Bitmap response) {
                art = response;
                album_art.setImageBitmap(response);         //set image with album art if it worked
            }
        }, 0, 0, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        AppVolleyState.instance().getRequestQueue().add(imageRequest);
    }


    public Runnable runnable = new Runnable() {         //runs every 30 seconds, refreshes song/artist and album art
        @Override
        public void run() {
            if (songEnd < Calendar.getInstance().getTimeInMillis() ) {
                album_art.setImageResource(R.drawable.cover_art_android);
            }
            getSongInfo();   //gets RSS data, which calls the getAlbumArtURL function, which calls the setAlbumArt function, refreshing the song/artist and album art
            handler.postDelayed(this, 30000);   //will run again in 30 seconds
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 30000);   //Runnable will run after 30000 milliseconds, or 30 seconds
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
