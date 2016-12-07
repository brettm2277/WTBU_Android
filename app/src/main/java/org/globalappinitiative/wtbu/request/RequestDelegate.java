package org.globalappinitiative.wtbu.request;

import com.android.volley.Response;
import com.android.volley.VolleyError;

/**
 * Created by calvin on 4/26/16.
 */
public abstract class RequestDelegate<T> implements Response.Listener<T>, Response.ErrorListener {

    @Override
    public void onErrorResponse(VolleyError error) {
    }

}
