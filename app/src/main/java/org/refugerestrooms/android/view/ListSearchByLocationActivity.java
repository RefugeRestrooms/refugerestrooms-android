package org.refugerestrooms.android.view;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.refugerestrooms.android.R;
import org.refugerestrooms.android.model.Bathroom;
import org.refugerestrooms.android.server.Server;
import org.refugerestrooms.android.server.Server.ServerListener;

import java.util.List;

public class ListSearchByLocationActivity extends ActionBarActivity implements ServerListener, LocationListener {
	public static final String INTENT_EXTRA_SEARCH_PARAMS = "location"; //TODO one of these for each search param

	private Server mServer;
    private ProgressBar progressBar;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Location lastKnownLocation;
    protected boolean gps_enabled,network_enabled;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_list_search);

            mServer = new Server(this);

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);

            // Create a progress bar to display while the list loads
            progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
            progressBar.setIndeterminate(true);

            // Must add the progress bar to the root of the layout
            ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
            root.addView(progressBar);

            Bundle extras = getIntent().getExtras();
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            String latLng = lastKnownLocation.getLatitude() + "," + lastKnownLocation.getLongitude();
            mServer.performSearch(latLng, true);
            Log.d("Captain's log", "latLng - " + latLng);
        }catch(Exception e){
            Log.d("Captain's log", e.getMessage());
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    return super.onCreateOptionsMenu(menu);
	}
	
	private void launchDetails(Bathroom bathroom) {
		//TODO add bathroom details
		Intent intent = new Intent(this, DetailViewActivity.class);
		intent.putExtra(DetailViewActivity.EXTRA_BATHROOM, bathroom.toJson());
		startActivity(intent);
	}

    //Listener for the server
    @Override
    public void onSearchResults(List<Bathroom> results) {
        ListView list = (ListView) findViewById(R.id.list_view);
        list.setEmptyView(findViewById(R.id.no_results));
        if(results != null) {
            ArrayAdapter<Bathroom> adapter = new BathroomListAdapter(getApplicationContext(), R.layout.list_entry, R.id.list_item_text, results);
            list.setAdapter(adapter);
        }else
            list.setAdapter(null);
        Log.d("Captain's log", "results - " + results);
        progressBar.setVisibility(ProgressBar.GONE);
    }


	@Override
	public void onSubmission(boolean success) {
		//nothing
	}

	
	@Override
	public void onError(final String errorMessage) {
		runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ListSearchByLocationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
	}

    @Override
    public void onLocationChanged(Location location) {
        if(lastKnownLocation != location) {
            String latLng = location.getLatitude() + "," + location.getLongitude();
            mServer.performSearch(latLng, true);
            Log.d("Captain's log", "onlocationchanged - " + latLng);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

        Log.d("Captain's log", "statusChanged");
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d("Captain's log", "onproviderenabled");

    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d("Captain's log", "onproviderdisabled");

    }

    public class BathroomListAdapter extends ArrayAdapter<Bathroom> {

		public BathroomListAdapter(Context applicationContext, int listEntry,
				int listItemText, List<Bathroom> results) {
			super(applicationContext, listEntry, listItemText, results);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final Bathroom bathroom = getItem(position);
			View view = super.getView(position, convertView, parent);
			BathroomSpecsViewUpdater.update(view, bathroom, getContext());
			if (bathroom != null) {
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						launchDetails(bathroom);
					}
				});
			}
			return view;
		}

	}

}
