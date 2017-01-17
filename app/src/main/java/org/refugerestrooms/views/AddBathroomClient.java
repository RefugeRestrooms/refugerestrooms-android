package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 7/14/2015.
 */

import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class AddBathroomClient extends WebViewClient {
    private String currentUrl;

    public AddBathroomClient() {
        super();
    }

    public AddBathroomClient(String currentUrl) {
        this.currentUrl = currentUrl;
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
        view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        if (url.equals(currentUrl)) {
            view.loadUrl(url);
        }
        view.loadUrl("http://www.refugerestrooms.org/restrooms/new?");
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
