package org.refugerestrooms.android.view;

import org.refugerestrooms.android.model.Bathroom;

import com.jmpumphrey.refugerestrooms.R;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageView;
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
	    
	    updateView();
	}

	private void updateView() {
		if (mBathroom != null) {
			TextView tv = (TextView) findViewById(R.id.text);
			tv.setText(getBathroomText());
			if (mBathroom.isAccessible()) {
				ImageView iv = (ImageView) findViewById(R.id.accessible);
				iv.setVisibility(View.VISIBLE);
			}
			if (mBathroom.isUnisex()) {
				ImageView iv = (ImageView) findViewById(R.id.unisex);
				iv.setVisibility(View.VISIBLE);
			}
		}
	}

	private CharSequence getBathroomText() {
		return mBathroom.getAddress().toString() + "\n" +
				"Directions: " + mBathroom.getDirections() + "\n" +
				"Comments: " + mBathroom.getComments();
	}
	
}
