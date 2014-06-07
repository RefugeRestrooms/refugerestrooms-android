package org.refugerestrooms.android.view;

import org.refugerestrooms.android.model.Bathroom;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jmpumphrey.refugerestrooms.R;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

public class DetailViewActivity extends ActionBarActivity {

	public static final String EXTRA_BATHROOM = "bathroom";
	private Bathroom mBathroom;
	
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
	    
	    updateView();
	}

	private void updateView() {
		if (mBathroom != null) {
			TextView tv = (TextView) findViewById(R.id.text);
			tv.setText(getBathroomText());
			View specsView = findViewById(R.id.specs);
			BathroomSpecsViewUpdater.update(specsView, mBathroom);

			updateMap();
		}
	}

	private void updateMap() {
		LatLng latLng = new LatLng(48, 0);
		GoogleMap map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
		if (map != null) {
			map.addMarker(new MarkerOptions().title(mBathroom.getName()).position(latLng));
		}
		map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		map.moveCamera(CameraUpdateFactory.zoomTo(15));
	}

	private CharSequence getBathroomText() {
		String address = "";
		for (int i = 0; i < mBathroom.getAddress().getMaxAddressLineIndex(); i++) {
			address += mBathroom.getAddress().getAddressLine(i) + "\n";
		}
		return address +
				"Directions: " + mBathroom.getDirections() + "\n\n" +
				"Comments: " + mBathroom.getComments();
	}
	
}
