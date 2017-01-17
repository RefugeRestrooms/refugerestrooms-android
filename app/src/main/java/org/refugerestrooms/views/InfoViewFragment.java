package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 9/26/15.
 * <p>
 * This is the detailed info view fragment which appears upon selecting the info icon, or by clicking on
 * the custom info window.
 */

import org.refugerestrooms.models.Bathroom;

import org.refugerestrooms.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;

public class InfoViewFragment extends android.support.v4.app.Fragment {

    public static final String EXTRA_BATHROOM = "bathroom";
    protected static final String TAG = InfoViewFragment.class.getSimpleName();
    private Bathroom mBathroom;
    public View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_details, container, false);

        Bundle args = getArguments();
        if (args != null) {
            mBathroom = Bathroom.fromJson(args.getString(EXTRA_BATHROOM));
        }
        // Creates a close button for the fragment
        final Button button = (Button) view.findViewById(R.id.close_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        ((ImageView) view.findViewById(R.id.button_maps)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInGoogleMaps();
            }
        });

        updateView();
        return view;

    }

    // Sets the TextView to display bathroom info within the fragment
    private void updateView() {
        if (mBathroom != null) {
            TextView tv = (TextView) view.findViewById(R.id.text_title);
            tv.setText(getBathroomTitle());
            TextView tv2 = (TextView) view.findViewById(R.id.text_address);
            tv2.setText(getBathroomAddress());
            TextView tv3 = (TextView) view.findViewById(R.id.text_comments);
            tv3.setText(Html.fromHtml((String) getBathroomComments()));
            tv.setGravity(Gravity.CENTER);
            tv2.setGravity(Gravity.CENTER);
            tv3.setGravity(Gravity.CENTER);
            // Gets specs such as bathroom rating, accessibility, and unisex properties
            View specsView = view.findViewById(R.id.specs);
            BathroomSpecsViewUpdater.update(specsView, mBathroom, getActivity());
        }
    }

    // Functions to retrieve info from bathroom object
    private CharSequence getBathroomTitle() {
        String text = "";
        String name = mBathroom.getName();
        if (!TextUtils.isEmpty(name)) {

            /* Had to re-decode bathroom name here for some reason... wouldn't work with same string used
             * in the customInfoWindow. Created a separate string variable in Bathroom.java
             * to hold off on decoding for the InfoViewFragment */
            //name = decodeString(name);
            text += name;
        }
        return text;
    }

    private CharSequence getBathroomAddress() {
        String text = "";
        String address = mBathroom.getAddress();
        if (!TextUtils.isEmpty(address)) {
            text += address;
        }
        text = getStringInBytes(text);
        return text;
    }

    private CharSequence getBathroomComments() {
        String text = "";
        String directions = mBathroom.getDirections();
        String comments = mBathroom.getComments();

        text += "<br><b>Directions</b><br><br>";
        if (!TextUtils.isEmpty(directions)) {
            text += directions + "<br><br>";
        } else {
            text += "No directions at this time.<br><br>";
        }
        text += "<br><b>Comments</b><br><br>";
        if (!TextUtils.isEmpty(comments)) {
            text += comments;
        } else {
            text += "No comments at this time.<br><br>";
        }
        text = getStringInBytes(text);
        return text;
    }

    // This is used to fix encoding errors from the API
    private static String getStringInBytes(String string) {
        try {
            string = new String(string.getBytes("UTF-8"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //e.printStackTrace();
        }
        return string;
    }

    private void openInGoogleMaps() {
        double lat = mBathroom.getmLatitude();
        double lon = mBathroom.getmLongitude();
        // Names need to be escaped, so a space should be replaced by either a + or by %20
        String addressEscaped = mBathroom.getAddress().replace(' ', '+');
        String uri = "geo:" + lat + "," + lon + "?q=" + addressEscaped;
        Uri intentUri = Uri.parse(uri);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }
}
