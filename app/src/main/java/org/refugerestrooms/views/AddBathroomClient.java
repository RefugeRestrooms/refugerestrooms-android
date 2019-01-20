package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 7/14/2015.
 */

import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddBathroomClient extends WebViewClient {
    private String currentUrl;

    public AddBathroomClient() {
        super();
    }

    public AddBathroomClient(String currentUrl) {
        this.currentUrl = currentUrl;
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
        // Removes header, footer -- areas where people can navigate away from the add a bathroom page
        view.loadUrl("javascript:(function() { " +
                "document.getElementsByTagName('header')[0].style.display='none'; " +
                "document.getElementsByTagName('footer')[0].style.display='none';" +
                "document.body.style.marginLeft=\"5%\";" +
                "document.body.style.marginRight=\"5%\";" +
                "document.body.style.backgroundColor=\"#e9e9e9\";" +
                "document.body.style.color=\"#8377AF\";" +
                "document.getElementsByTagName('h5')[0].style.display='none';" +
                "document.getElementsByClassName('guess-btn')[0].style.display='none';" +
                "})()");
        // Time Delay to prevent display showing before javascript finishes
        view.postDelayed(new Runnable() {
            public void run() {
                view.setVisibility(View.VISIBLE);
            }
        }, 50);

    }
}
