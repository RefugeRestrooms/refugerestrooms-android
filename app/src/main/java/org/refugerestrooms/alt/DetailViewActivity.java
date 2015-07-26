package org.refugerestrooms.alt;

import org.refugerestrooms.models.Bathroom;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import org.refugerestrooms.R;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class DetailViewActivity extends ActionBarActivity {

	public static final String EXTRA_BATHROOM = "bathroom";
    protected static final String TAG =  DetailViewActivity.class.getSimpleName();
	private Bathroom mBathroom;
    protected GoogleMap map;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);

	    ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    if (getIntent().getExtras() != null) {
	    	mBathroom = Bathroom.fromJson(getIntent().getExtras().getString(EXTRA_BATHROOM));
	    }
	    
	    String name = mBathroom.getName();
		if (name != null) {
	    	setTitle(name);
	    }
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
	    updateView();
	}

	private void updateView() {
		if (mBathroom != null) {
			TextView tv = (TextView) findViewById(R.id.text);
			tv.setText(getBathroomText());
			View specsView = findViewById(R.id.specs);
			BathroomSpecsViewUpdater.update(specsView, mBathroom, this);

			updateMap();
		}
	}

    @Override
    public void onBackPressed(){
        //Your code here
        Log.d(TAG, "On Back Pressed");
        super.onBackPressed();
    }

	private void updateMap() {
		LatLng latLng = mBathroom.getLocation();
		if (map != null) {
			map.addMarker(new MarkerOptions().title(mBathroom.getName()).position(latLng));
            map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		    map.moveCamera(CameraUpdateFactory.zoomTo(15));
        }
	}

	private CharSequence getBathroomText() {
		String text = "";
		String address = mBathroom.getAddress();
		String directions = mBathroom.getDirections();
		String comments = mBathroom.getComments();
		if (!TextUtils.isEmpty(address)) {
			text += address;
		}
		if (!TextUtils.isEmpty(directions)) {
			text += "\nDirections: " + directions + "\n";
		}
		if (!TextUtils.isEmpty(comments)) {
			text += "\nComments: " + comments;
		}
		
		return  text;
	}

	
}

