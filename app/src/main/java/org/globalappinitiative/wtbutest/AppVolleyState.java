package org.globalappinitiative.wtbutest;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by calvin on 4/25/16.
 *
 * This is just a wrapper for some global state around Volley, namely a global RequestQueue.
 * Use it like so: AppVolleyState.instance().getRequestQueue().add(myRequest);
 */
public class AppVolleyState {

    private static AppVolleyState state = null;

    private RequestQueue requestQueue;

    public static void initInstance(Context c) {
        state = new AppVolleyState(c);
    }

    public static AppVolleyState instance() {
        return state;
    }

    private AppVolleyState(Context c) {
        requestQueue = Volley.newRequestQueue(c);
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public void quickRequest(String url, Map<String, String> params, SquickRequestDelegate delegate) {
        requestQueue.add(new JSONGETRequest(url, params, delegate, delegate));
    }

    // Static class
    public static abstract class SquickRequestDelegate implements Response.Listener<JSONObject>, Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("VolleyError", error.toString());
        }

    }

    // Static aliases

    public static RequestQueue sgetGetRequestQueue() {
        return instance().getRequestQueue();
    }

    public static void squickRequest(String url, Map<String, String> params, SquickRequestDelegate delegate) {
        instance().quickRequest(url, params, delegate);
    }

}
