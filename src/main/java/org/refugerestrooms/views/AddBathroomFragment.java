package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 7/14/2015.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.refugerestrooms.R;

public class AddBathroomFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.add_bathroom, container, false);
        WebView mWebView = (WebView) rootView.findViewById(R.id.addBathroom);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new AddBathroomClient("http://www.refugerestrooms.org/restrooms/new?"));
        mWebView.loadUrl("http://www.refugerestrooms.org/restrooms/new?");
        // Inflate the layout for this fragment
        return rootView;
    }
}

