package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 7/14/2015.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class AddBathroomClient extends WebViewClient {
    private String currentUrl;
    private Context context;
    private WebView view;
    private Location mCurrentLocation;

    public AddBathroomClient() {
        super();
    }

//    public AddBathroomClient(String currentUrl) {
//        this.currentUrl = currentUrl;
//    }

    // Markus: why are you using view here but WebView everywhere else in the code?
    public AddBathroomClient(String currentUrl, Context context, WebView view, Location mCurrentLocation) {
        this.currentUrl = currentUrl;
        this.context = context;
        this.view = view;
        this.mCurrentLocation = mCurrentLocation;
        this.view.addJavascriptInterface(new JavaScriptInterface(context, this), "AndroidInterface");
    }

    /**
     * Tests if a connection can be made with Google.
     *
     * @return true if a connection was successful, false otherwise.
     */
    private boolean hasInternetConnection() {
        try {
            // Test internet connection
            HttpURLConnection urlConnection = (HttpURLConnection)
                    (new URL("http://clients3.google.com/generate_204")
                            .openConnection());
            urlConnection.setRequestProperty("User-Agent", "Android");
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setConnectTimeout(1500);
            urlConnection.connect();
            if (urlConnection.getResponseCode() == 204 &&
                    urlConnection.getContentLength() == 0) {
                // Successfully connected to the internet.
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            // No internet connection detected, or an error has been encountered.
            return false;
        }

    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        /* Check to see if url equals the add a restroom page, otherwise reloads page and shows message
         saying that the bathroom was submitted successfully (only navigation away from page is the submit button).
         Bathrooms are submitted correctly, but should probably do this without a text_directions. Note: the
         restroom submitted successfully message is from the refuge restrooms site, not the app.
         */
        // Setting the render thread priority is deprecated and will not be supported
        // view.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);

        // Pre-loading the webview requires using the cache for now.
        // Check for an internet connection so the website won't load without it.

        // Causing crashes presently, replace with a proper volley HTTP Request later
        /*if (!hasInternetConnection()) {
            //TODO Add no connection detected page to display here.
            return false;
        }*/
        // view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (url.equals(currentUrl)) {
            view.loadUrl(url);
        }
        view.loadUrl("https://www.refugerestrooms.org/restrooms/new?");
        return true;
    }

    @Override
    public void onPageFinished(final WebView view, String url) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        // Removes header, footer -- areas where people can navigate away from the add a bathroom page
        view.evaluateJavascript("javascript:(function() { " +
                "document.getElementsByTagName('header')[0].style.display='none'; " +
                "document.getElementsByTagName('footer')[0].style.display='none';" +
                "document.body.style.marginLeft=\"5%\";" +
                "document.body.style.marginRight=\"5%\";" +
                "document.body.style.backgroundColor=\"#e9e9e9\";" +
                "document.body.style.color=\"#8377AF\";" +
                "document.getElementByID('restroom_street')[0].style.display='none';" +
                "document.getElementByID('restroom_street')[0].style.display='none';" +
                "document.getElementByID('restroom_city')[0].style.display='none';" +
                "document.getElementByID('restroom_state')[0].style.display='none';" +
                "})()", null);


        // Add the function to the button
        view.evaluateJavascript("javascript:(function() { " +
                "var button = document.getElementsByClassName('guess-btn')[0];" +
                "if (button) { console.log('Button found'); } else { console.log('Button not found'); }" +
                "button.onclick = function(event) {" +
                "   event.preventDefault();" + // Prevent the default behavior of the button
                "   AndroidInterface.getLocationAddress(" + mCurrentLocation.getLatitude() + ", " + mCurrentLocation.getLongitude() + ");" +
                "};" +
                "})()", null);

        // Time Delay to prevent display showing before javascript finishes
        view.postDelayed(new Runnable() {
            public void run() {
                view.setVisibility(View.VISIBLE);
            }
        }, 50);
    }



    /**
     * Gets the location address from latitude and longitude and updates the input fields
     * in the WebView with the address information.
     *
     * @param latitude  The latitude of the location.
     * @param longitude The longitude of the location.
     */
    public void getLocationAddress(double latitude, double longitude) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                final String address = addresses.get(0).getAddressLine(0);
                final String[] addressParts = address.split(",");

                // Print the address to the WebView
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        view.evaluateJavascript("javascript:(function() { " +
                                "var inputElement = document.getElementById('restroom_street');" +
                                "if (inputElement) {" +
                                "    inputElement.value = '" + addressParts[0] + "';" +
                                "} else {" +
                                "    console.log('Input element with ID \\'restroom_street\\' not found.');" +
                                "}" +
                                "var inputElement = document.getElementById('restroom_city');" +
                                "if (inputElement) {" +
                                "    inputElement.value = '" + addressParts[1] + "';" +
                                "} else {" +
                                "    console.log('Input element with ID \\'restroom_city\\' not found.');" +
                                "}" +
                                "var inputElement = document.getElementById('restroom_state');" +
                                "if (inputElement) {" +
                                "    inputElement.value = '" + addressParts[2] + "';" +
                                "} else {" +
                                "    console.log('Input element with ID \\'restroom_state\\' not found.');" +
                                "}" +
                                "})()", null);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}



