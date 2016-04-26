package org.globalappinitiative.wtbutest;

import android.net.Uri;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by calvin on 4/25/16.
 *
 * A custom json request that is tuned for the API.
 */
public class JSONGETRequest extends Request<JSONObject> {

    private Response.Listener<JSONObject> listener;

    private static String buildUponURL(String url, Map<String, String> params) {
        Uri.Builder builder = Uri.parse(url).buildUpon();
        for (Map.Entry<String, String> param : params.entrySet()) {
            builder.appendQueryParameter(param.getKey(), param.getValue());
        }
        return builder.build().toString();
    }

    public JSONGETRequest(String url, Map<String, String> params,
                          Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        super(Request.Method.GET, buildUponURL(url, params), errorListener);
        this.listener = responseListener;
        setRetryPolicy(new CustomRetryPolicy());

    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        listener.onResponse(response);
    }

    private class CustomRetryPolicy implements RetryPolicy {
        @Override
        public int getCurrentTimeout() {
            return 50000;
        }

        @Override
        public int getCurrentRetryCount() {
            return 50000;
        }

        @Override
        public void retry(VolleyError error) throws VolleyError {

        }
    }

}
