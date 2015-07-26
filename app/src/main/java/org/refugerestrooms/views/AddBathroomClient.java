package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 7/14/2015.
 */

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AddBathroomClient extends WebViewClient {
    private String currentUrl;

    public AddBathroomClient(String currentUrl){
        this.currentUrl = currentUrl;
    }
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        /* Check to see if url equals the add a restroom page, otherwise reloads page and shows message
         saying that the bathroom was submitted successfully (only navigation away from page is the submit button).
         Bathrooms are submitted correctly, but should probably do this without a text_directions. Note: the
         restroom submitted successfully message is from the refuge restrooms site, not the app.
         */
        if(url.equals(currentUrl)) {
            view.loadUrl(url);
        }
        view.loadUrl("http://www.refugerestrooms.org/restrooms/new?");
        return true;
    }

    // Removes header, footer -- areas where people can navigate away from the add a bathroom page
    @Override
    public void onPageFinished(WebView view, String url) {
        view.loadUrl("javascript:(function() { " +
            "document.getElementsByTagName('header')[0].style.display='none'; " +
            "document.getElementsByTagName('footer')[0].style.display='none';" +
                "})()");
    }
}
