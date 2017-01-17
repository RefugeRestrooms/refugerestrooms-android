package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 7/13/2015.
 */

import java.io.IOException;
import java.io.InputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.refugerestrooms.R;
import org.refugerestrooms.servers.JavaScriptInterface;

public class TextDirectionsActivity extends AppCompatActivity {
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_directions);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mWebView = (WebView) findViewById(R.id.webView1);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:calcRoute()");
                mProgressBar.setVisibility(View.GONE);
            }
        });
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JavaScriptInterface(this), "Android");

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.getIndeterminateDrawable().setColorFilter(
                getResources().getColor(R.color.ColorAccent), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onResume() {
        super.onResume();
        //load from html file in assets
        InputStream is;
        String html = new String();
        try {
            is = getAssets().open("directions.html");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            html = new String(buffer);
            is.close();
        } catch (IOException e) {
            //e.printStackTrace();
        }

        // Gets location that was passed into text_directions
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String start = extras.getString("START_LOC", "");
            String end = extras.getString("END_LOC", "");
            String title = extras.getString("TITLE", "");
            mToolbar.setSubtitle(title);

            html = html.replace("$START$", start);
            html = html.replace("$END$", end);
        } else {
            //temp fix for no location
            html = html.replace("$START$", "Walter Library UMN");
            html = html.replace("$END$", "Coffman Memorial Union");
            Snackbar.make(mToolbar, "Location Not Enabled.. Setting Randon Location",
                    Snackbar.LENGTH_SHORT).show();
        }
        //Loads the given data into this WebView, using baseUrl as the base URL for the content -- defaults to about:blank
        //The base URL is used both to resolve relative URLs and when applying JavaScript's same origin policy.
        //The historyUrl is used for the history entry -- defaults to about:blank
        mWebView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }
}
