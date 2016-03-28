package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 7/13/2015.
 */
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class MyWebViewClient extends WebViewClient {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        //put javascript file here
        //use this function for hiding elements instead?
        //view.loadUrl("javascript:document.getElementByClassName('example')")

        //change travel mode to walking in html!!
        //uncomment event dom watcher in html?? also cannot read event property of null 35 html
        view.loadUrl("javascript:calcRoute()");
    }
}
