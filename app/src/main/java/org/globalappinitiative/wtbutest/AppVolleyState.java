package org.globalappinitiative.wtbutest;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.globalappinitiative.wtbutest.request.GetArrayRequest;
import org.globalappinitiative.wtbutest.request.GetObjectRequest;
import org.globalappinitiative.wtbutest.request.RequestDelegate;
import org.json.JSONArray;
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

    public void objectRequest(String url, Map<String, String> params, RequestDelegate<JSONObject> delegate) {
        requestQueue.add(new GetObjectRequest(url, params, delegate, delegate));
    }

    public void arrayRequest(String url, Map<String, String> params, RequestDelegate<JSONArray> delegate) {
        requestQueue.add(new GetArrayRequest(url, params, delegate, delegate));
    }

    // Static aliases

    public static void sobjectRequest(String url, Map<String, String> params, RequestDelegate<JSONObject> delegate) {
        instance().objectRequest(url, params, delegate);
    }

    public static void sarrayRequest(String url, Map<String, String> params, RequestDelegate<JSONArray> delegate) {
        instance().arrayRequest(url, params, delegate);
    }

}
