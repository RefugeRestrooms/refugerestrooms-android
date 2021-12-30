package org.refugerestrooms.views;
import android.content.Context;

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

    private WebView mWebView;
    private Bundle mWebViewBundle;
    private String editParams;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Pre-load the website into the cache when the fragment is first attached.
        WebView preWebView = new WebView(context);
        preWebView.loadUrl("https://www.refugerestrooms.org/restrooms/new?" + editParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_bathroom, container, false);
        mWebView = (WebView) rootView.findViewById(R.id.addBathroom);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new AddBathroomClient("https://www.refugerestrooms.org/restrooms/new?" + editParams));

        // If possible, restore the WebView state - otherwise load the new restroom page
        if (mWebViewBundle != null) {
            mWebView.restoreState(mWebViewBundle);
        } else {
            mWebView.loadUrl("https://www.refugerestrooms.org/restrooms/new?" + editParams);
        }

        // Inflate the layout for this fragment
        return rootView;
    }

    /**
     * Save the WebView's state when the application pauses (e.g. on an orientation change)
     */
    @Override
    public void onPause() {
        super.onPause();
        mWebViewBundle = new Bundle();
        mWebView.saveState(mWebViewBundle);
    }

    public void setEditId(String editId) {
        this.editParams = editId != null ? "edit_id=" + editId + "&id=" + editId : "";
        mWebViewBundle = null;
    }
}
