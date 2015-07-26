package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 7/13/2015.
 */
import java.io.IOException;
import java.io.InputStream;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import org.refugerestrooms.R;
import org.refugerestrooms.servers.JavaScriptInterface;


public class TextDirectionsActivity extends ActionBarActivity {
    private WebView webView;
    final MyWebViewClient myWebViewClient = new MyWebViewClient();
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_directions);
        //setWebContentsDebuggingEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        webView = (WebView) findViewById(R.id.webView1);
        //webView.getSettings().setJavaScriptEnabled(true);
        //webView.loadUrl("http://www.google.com");
        webView.setWebViewClient(myWebViewClient);
        //this call requires api 11??
        //webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        WebSettings webSettings = webView.getSettings();
        //next line maybe not needed
        //webSettings.setUseWideViewPort(false);
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JavaScriptInterface(this), "Android");


        //load from html file in assets
        InputStream is = null;
        try {
            is = getAssets().open("directions.html");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int size = 0;
        try {
            size = is.available();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Read the entire asset into a local byte buffer.
        byte[] buffer = new byte[size];
        try {
            is.read(buffer);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Convert the buffer into a string.
        String html = new String(buffer);

        // Gets location that was passed into text_directions
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String start = extras.getString("START_LOC");
            String end = extras.getString("END_LOC");
            setTitle(extras.getString("TITLE"));

            html = html.replace("$START$", start);
            html = html.replace("$END$", end);
        }
        else {
            //temp fix for no location
            html = html.replace("$START$", "Walter Library UMN");
            html = html.replace("$END$", "Coffman Memorial Union");
            Toast.makeText(this,"Location Not Enabled.. Setting Random Location",
                    Toast.LENGTH_SHORT).show();
        }
        //Loads the given data into this WebView, using baseUrl as the base URL for the content -- defaults to about:blank
        //The base URL is used both to resolve relative URLs and when applying JavaScript's same origin policy.
        //The historyUrl is used for the history entry -- defaults to about:blank
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.finish();
                //NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }
}
