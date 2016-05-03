package org.refugerestrooms.views;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.refugerestrooms.R;

import java.io.UnsupportedEncodingException;

/**
 * @author David Mascharka (davidmascharka@gmail.com)
 */
public class BathroomInfoWindow implements GoogleMap.InfoWindowAdapter {

    private View mInfoWindowView;

    // TODO change this - ActionBarActivity is deprecated
    public BathroomInfoWindow(ActionBarActivity activity) {
        mInfoWindowView = activity.getLayoutInflater().inflate(R.layout.bathroom_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        return null;
    }

    // Defines the contents of the InfoWindow
    //title, picture, snippet, score, accessible
    @Override
    public View getInfoContents(Marker arg0) {
        // Getting the position from the marker
        String title = arg0.getTitle();
        String snippet = arg0.getSnippet();

        // Getting references to the TextViews to set title and address snippet
        TextView windowTitle = (TextView) mInfoWindowView.findViewById(R.id.window_title);
        TextView windowSnippet = (TextView) mInfoWindowView.findViewById(R.id.window_snippet);

        // Getting references to the ImageViews to set unisex and accessibility
        ImageView windowAccessible = (ImageView) mInfoWindowView.findViewById(R.id.accessible);
        ImageView windowUnisex = (ImageView) mInfoWindowView.findViewById(R.id.unisex);

        // Hide accessible, unisex logos by default
        windowAccessible.setVisibility(View.GONE);
        windowUnisex.setVisibility(View.GONE);

        // get accessible, unisex logos
        // Encoded in the snippet
        int idx = snippet.indexOf("*");
        int isAccessible = Integer.parseInt(snippet.substring(idx+1, idx+2));
        int isUnisex = Integer.parseInt(snippet.substring(idx+2, idx+3));
        snippet = snippet.substring(0, idx);

        if (isAccessible == 1) {
            windowAccessible.setVisibility(View.VISIBLE);
        }
        if (isUnisex == 1) {
            windowUnisex.setVisibility(View.VISIBLE);
        }

        // Setting the title
        title= getStringInBytes(title);
        windowTitle.setText(title);
        windowTitle.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        // Setting the directions snippet
        windowSnippet.setText(snippet);

        // Returning the view containing InfoWindow contents
        return mInfoWindowView;
    }
    // This is used to fix encoding errors from the API
    private String getStringInBytes(String string) {
        try {
            string = new String(string.getBytes("UTF-8"),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
        }
        return string;
    }
}
