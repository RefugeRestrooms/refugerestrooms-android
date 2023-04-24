package org.refugerestrooms.views;

import android.content.Context;
import android.webkit.JavascriptInterface;

public class JavaScriptInterface {
    private Context mContext;
    private AddBathroomClient mAddBathroomClient;

    public JavaScriptInterface(Context context, AddBathroomClient addBathroomClient) {
        mContext = context;
        mAddBathroomClient = addBathroomClient;
    }

    @JavascriptInterface
    public void getLocationAddress(double latitude, double longitude) {
        mAddBathroomClient.getLocationAddress(latitude, longitude);
    }
}