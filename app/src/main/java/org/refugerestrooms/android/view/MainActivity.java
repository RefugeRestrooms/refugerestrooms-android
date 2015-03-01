package org.refugerestrooms.android.view;

import org.refugerestrooms.android.R;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;
import android.text.util.Linkify;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends ActionBarActivity implements LocationListener {
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Double latitude,longitude;
    protected boolean gps_enabled,network_enabled;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }
	
	/**
	 * Launch the Add loo activity
	 * @param view
	 */
	public void onClickAdd(View view) {
		Intent intent = new Intent(this, AddActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Search by location
	 * @param view
	 */
	public void onClickLocation(View view) {
		Intent intent = new Intent(this, AddActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Launch the about activity
	 * @param view
	 */
	public void onClickAbout(View view) {
 		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	/**
	 * Launch the Search by location activity
	 * @param view
	 */
	public void onClickSearchByLocation(View view) {
        Intent intent = new Intent(this, ListSearchActivity.class);
        String searchLocation = latitude+ "," + longitude;
        intent.putExtra(ListSearchActivity.INTENT_EXTRA_LOCATION_PARAMS, searchLocation);
        startActivity(intent);
	}

	/**
	 * Launch the Search activity
	 * @param view
	 */
	public void onClickSearch(View view) {
		Intent intent = new Intent(this, ListSearchActivity.class);
		EditText searchField = (EditText) findViewById(R.id.searchText);
		String searchTerm = searchField.getText().toString();
		intent.putExtra(ListSearchActivity.INTENT_EXTRA_SEARCH_PARAMS, searchTerm);
		startActivity(intent);
	}

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
