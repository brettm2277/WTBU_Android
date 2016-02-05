package org.globalappinitiative.wtbutest;

import android.app.Application;
import android.os.Bundle;

import com.parse.Parse;

/**
 * Created by BrettM on 11/10/2015.
 */
public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "1YQrSQX8ISBBkVdXA2tgSmv0j2cBOx878Es5d5lD", "zplY28RZTzs5SqiUog33vcDlCIqP7FaJcVS28daA");
    }
}
