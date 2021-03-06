package org.globalappinitiative.wtbu.request;

import android.net.Uri;

import java.util.Map;

/**
 * Created by calvin on 4/26/16.
 */
class RequestHelper {

    // This class adds parameters to urls so that specifying thinks like tokens and
    // other get request parameters is easier. Volley only does this by default for
    // POST and PUT requests.
    public static String buildUponURL(String url, Map<String, String> params) {
        if (params == null) {
            return url;
        }
        Uri.Builder builder = Uri.parse(url).buildUpon();
        for (Map.Entry<String, String> param : params.entrySet()) {
            builder.appendQueryParameter(param.getKey(), param.getValue());
        }
        return builder.build().toString();
    }

}
