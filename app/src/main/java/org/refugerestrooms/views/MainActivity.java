package org.refugerestrooms.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.refugerestrooms.R;
import org.refugerestrooms.models.Bathroom;
import org.refugerestrooms.models.Haversine;
import org.refugerestrooms.models.ListOfBathrooms;
import org.refugerestrooms.servers.JsonRequest;
import org.refugerestrooms.servers.Server;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Character.getNumericValue;

//TODO ActionBarActivity has been depreciated... use toolbar instead
public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        RoutingListener, Server.ServerListener {

    private GoogleMap mMap = null;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final String TAG = "Refuge Restrooms";
    private Boolean initial = true;

    Location mCurrentLocation;
    Location mLastLocation;
    Location mCurrentLocationNoGps;
    LocationManager locationManager;
    LatLng currentPosition;
    boolean mUpdatesRequested;
    private boolean mInProgress;
    public boolean doNotDisplayDialog = false;
    public boolean onSearchAction = false;
    protected LatLng start;
    protected LatLng end;
    // temp lat/lng for setting up initial map
    static final LatLng COFFMAN  	       = new LatLng(44.972905,-93.235613);

    private int numLocations;
    SharedPreferences mPrefs;
    SharedPreferences.Editor mEditor;
    public enum REQUEST_TYPE {START, STOP}
    private REQUEST_TYPE mRequestType;

    // Global constants
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 3;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    // Update User's activity (Driving, biking, etc ...)
    public static final int ACTIVITY_INTERVAL_SECONDS = 20;
    public static final int ACTIVITY_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * ACTIVITY_INTERVAL_SECONDS;
    /*
     * Store the PendingIntent used to send activity recognition events
     * back to the app
     */
    private PendingIntent mActivityRecognitionPendingIntent;
    // Store the current activity recognition client
    //private ActivityRecognitionClient mActivityRecognitionClient;
    private static Context mContext = null;

    //LocationRequest mLocationRequest;

	/*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */

    // Check for Google Play Services
    private final static int
            CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;
        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }
        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {

            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                        break;
                }

        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
          //  Log.d("Location Updates",
          //          "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error code
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode,
                    this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment =
                        new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(),
                        "Location Updates");
            }
            return false;
        }
    }

    // Location services callback
    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */

    @Override
    public void onConnected(Bundle dataBundle) {
        if (servicesConnected()) {
            // Display the connection status
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
            // If already requested, start periodic updates
            // 3rd parameter just (this)?
            if (mUpdatesRequested) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
            }
            //TODO debug this part when no wifi/gps/mobile

            // Get the current location and move camera to it
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mCurrentLocation == null) {
                //Log.d("","provider : "+ provider);
                // String provider = LocationManager.GPS_PROVIDER;
                LocationListener locationListener = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        // Called when a new location is found by the network location provider.
                        mCurrentLocationNoGps = location;
                    }
                    public void onStatusChanged(String provider, int status, Bundle extras) {}

                    public void onProviderEnabled(String provider) {}

                    public void onProviderDisabled(String provider) {}
                };
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            }

            /*******************************************************************
             * API call to Refuge Restrooms here
             *
             **************************************************************/

            // Gets bathroom data from RefugeRestrooms.org (20 closest entries -- defined in Server.java)
            String curLatLng;
            if (mCurrentLocation != null) {

                double tmpLat = mCurrentLocation.getLatitude();
                double tmpLng = mCurrentLocation.getLongitude();

                // String manipulation here to get in the right format for API call
                curLatLng = "lat=" + Double.toString(tmpLat) + "&lng=" + Double.toString(tmpLng);

                mServer = new Server(this);
                mServer.performSearch(curLatLng, true);
            }

            else {
                //TODO get nearby location when GPS is disabled -- currently crashing, so it's been set to Minnesota
                // If no location info, sets LatLng to be Coffman Memorial Union (temp fix)
                //curLatLng = "lat=44.9727&lng=-93.2354";
                /*
                curLatLng = "Minneapolis, MN";
                mServer = new Server(this);
                mServer.performSearch(curLatLng, false); */
            }

            /*******************************************************************
             * End of API call to Refuge Restrooms
             *
             **************************************************************/

            /**
             * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
             */
            if (mCurrentLocation != null) {
                double myLat = mCurrentLocation.getLatitude();
                double myLng = mCurrentLocation.getLongitude();
                currentPosition = new LatLng(myLat,myLng);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));

                // Starts directions from location routing library
                start = currentPosition;
            }
        }
    }

	/*
	 * Called by Location Services if the connection to the
	 * location client drops because of an error.
	 */
	/*
	@Override
	public void onDisconnected() {
	    // Display the connection status
	    Toast.makeText(this, "Disconnected. Please re-connect.",
	            Toast.LENGTH_SHORT).show();
	    // Turn off the request flag
        mInProgress = false;
        // Delete the client
       // mActivityRecognitionClient = null;
	}
	*/

    public Dialog createDialog() {
        LayoutInflater inflater = this.getLayoutInflater();
        View neverShow = inflater.inflate(R.layout.never_show, null);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(neverShow)
                .setTitle(R.string.location_settings_title)
                .setMessage(R.string.location_instructions)
                .setPositiveButton(R.string.location_settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton(R.string.location_skip, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void doNotShowAgain() {
        // Persist shared preference to prevent dialog from showing again.
       // Log.d("MainActivity", "TODO: Persist shared preferences.");
    }
    public void onConnectionSuspended(int i) {
       // Log.i(TAG, "GoogleApiClient connection has been suspend");
    }
    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
	    /*
	     * Google Play services can resolve some errors it detects.
	     * If the error has a resolution, try sending an Intent to
	     * start a Google Play services activity that can resolve
	     * error.
	     */
        // Turn off the request flag
        mInProgress = false;
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
	            /*
	             * Thrown if Google Play services canceled the original
	             * PendingIntent
	             */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                //e.printStackTrace();
            }
        } else {
	        /*
	         * If no resolution is available, display a dialog to the
	         * user with the error.
	         */
            showErrorDialog(connectionResult.getErrorCode());
            /**
             //Get the error code
             int errorCode = connectionResult.getErrorCode();
             // Get the error dialog from Google Play services
             Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
             errorCode,
             this,
             CONNECTION_FAILURE_RESOLUTION_REQUEST);
             // If Google Play services can provide an error dialog
             if (errorDialog != null) {
             // Create a new DialogFragment for the error dialog
             ErrorDialogFragment errorFragment =
             new ErrorDialogFragment();
             // Set the dialog in the DialogFragment
             errorFragment.setDialog(errorDialog);
             // Show the error dialog in the DialogFragment
             errorFragment.show(
             getSupportFragmentManager(),
             "Activity Recognition");
             */
        }
    }
    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
    }

    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private String mLocationTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // For search results
        handleIntent(getIntent());
        // Checks if gps is enabled, kicks out message to turn on if not.
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        android.location.LocationListener locationListener = new android.location.LocationListener(){
            @Override
            public void onLocationChanged(Location location) {
                if (location != null)
                {
                  //  Log.i("SuperMap", "Location changed : Lat: " + location.getLatitude() + " Lng: " + location.getLongitude());
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                  //  Log.i("latitude,longitude", ""+latitude+","+longitude);
                    mCurrentLocation = location;
                }
            }

            public void onProviderDisabled(String provider) {}
            public void onProviderEnabled(String provider) {}
            public void onStatusChanged(String provider, int status, Bundle extras) {}
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
          //  Log.e("MainActivity", ex.getMessage());
        }

        if (!gps_enabled) {
            // Added if statement to prevent dialog box from re-showing on search
            if (!doNotDisplayDialog) {
                Dialog dialog = createDialog();
                dialog.show();
            }

            // Tries to get data from network otherwise
            try {
                network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
              //  Log.e("MainActivity", ex.getMessage());
            }
        }
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
		/*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */

        if (servicesConnected()) {
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);
            // Disables the get directions from google maps icons (this would open the Maps app)
            //mMap.getUiSettings().setMapToolbarEnabled(false);
            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create();
            // Location Accuracy
            mLocationRequest.setPriority(
                    LocationRequest.PRIORITY_HIGH_ACCURACY);
            // LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            // Set the update interval to 5 seconds
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            // Set the fastest update interval to 1 second
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

            // Open the shared preferences
            mPrefs = getSharedPreferences("SharedPreferences",
                    Context.MODE_PRIVATE);
            // Get a SharedPreferences editor
            mEditor = mPrefs.edit();
	        /*
	         * Create a new location client, using the enclosing class to
	         * handle callbacks.
	         */

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            // Start with updates turned off
            mUpdatesRequested = false;
            mContext = getApplicationContext();

            final Context context = this;
            getSupportActionBar().setTitle("Refuge Restrooms");
        }
        /** Swaps fragments in the main content view */
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /**
             * Called when a drawer has settled in a completely closed state.
             */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /**
             * Called when a drawer has settled in a completely open state.
             */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }
    // For search activity in action bar
    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            // Use the query to search
            Server mServer = new Server(this);
            // Boolean to prevent "gps not enabled" dialog box from re-showing on search
            doNotDisplayDialog = true;
            onSearchAction = true;
            mServer.performSearch(query, false);
        }
    }

    /** Swaps fragments in the main content view */

    @Override
    public void onRoutingFailure() {
        // The Routing request failed
    }

    @Override
    public void onRoutingStart() {
        // The Routing Request starts
    }

    Polyline poly1;
    Polyline poly2;
    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        //removes polyline on update to create new one
        if (poly1 != null){
            poly1.remove();
        }
        if (poly2 != null){
            poly2.remove();
        }
        PolylineOptions polyline_outline = new PolylineOptions();
        polyline_outline.color(Color.rgb(50, 15, 255)); //dark blue
        polyline_outline.width(20);
        polyline_outline.addAll(mPolyOptions.getPoints());
        poly1 = mMap.addPolyline(polyline_outline);

        PolylineOptions polyline = new PolylineOptions();
        polyline.color(Color.rgb(99, 125, 255)); //light blue
        polyline.width(10);
        polyline.addAll(mPolyOptions.getPoints());
        poly2 = mMap.addPolyline(polyline);

        // Start marker
        //MarkerOptions options = new MarkerOptions();
	  /*options.position(start);
	  options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
	  mMap.addMarker(options);
	*/
        // End marker
	  /*options = new MarkerOptions();
	  options.position(end);
	  options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
	  mMap.addMarker(options);
	*/
    }
    /*
     * Called when the Activity becomes visible.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
        // Creating a new Location from test data
    }

    @Override
    protected void onPause() {
        // Save the current setting for updates
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        /*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested =
                    mPrefs.getBoolean("KEY_UPDATES_ON", false);

            // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }
    }
    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // If the client is connected
        if (mGoogleApiClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            //mGoogleApiClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
        mGoogleApiClient.disconnect();
        super.onStop();
    }
    // Define the callback method that receives location updates
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        if (location != null) {
            String msg = "Updated Location: " +
                    Double.toString(location.getLatitude()) + "," +
                    Double.toString(location.getLongitude());
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            mCurrentLocation = location;
        }
        //have to convert from location to LatLng
        //LatLng pos = new LatLng(location.getLatitude(),location.getLongitude());
        //pos_marker.setPosition(pos);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /*
     * Request activity recognition updates based on the current
     * detection interval.
     *
     */
    public void startUpdates() {
        // Set the request type to START
        mRequestType = REQUEST_TYPE.START;

        // Check for Google Play services
        if (!servicesConnected()) {
            return;
        }
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            // mActivityRecognitionClient.connect();
            //
        } else {
           /*
            * A request is already underway. You can handle
            * this situation by disconnecting the client,
            * re-setting the flag, and then re-trying the
            * request.
            */

            // mActivityRecognitionClient.disconnect();
            mInProgress = false;
            startUpdates();
        }
    }
    public void stopUpdates() {
        // Set the request type to STOP
        mRequestType = REQUEST_TYPE.STOP;
        /*
         * Test for Google Play services after setting the request type.
         * If Google Play services isn't present, the request can be
         * restarted.
         */
        if (!servicesConnected()) {
            return;
        }
        // If a request is not already underway
        if (!mInProgress) {
            // Indicate that a request is in progress
            mInProgress = true;
            // Request a connection to Location Services
            //   mActivityRecognitionClient.connect();
            //
        }
        else {
            /*
             * A request is already underway. You can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }
    public class BathroomListAdapter extends ArrayAdapter<Bathroom> {

        public BathroomListAdapter(Context applicationContext, int listEntry,
                                   int listItemText, List<Bathroom> results) {
            super(applicationContext, listEntry, listItemText, results);
        }
    }

    private Server mServer;
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(COFFMAN, 15));
            mMap.getUiSettings().setZoomControlsEnabled(false);

            // Custom info window
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                // Use default InfoWindow frame
                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                // Defines the contents of the InfoWindow
                //title, picture, snippet, score, accessible
                @Override
                public View getInfoContents(Marker arg0) {

                    // Getting view from the layout file
                    View v = getLayoutInflater().inflate(R.layout.custom_info_window, null);

                    // Getting the position from the marker
                    String title = arg0.getTitle();
                    String snippet = arg0.getSnippet();

                    // Getting reference to the TextView to set title
                    TextView windowTitle = (TextView) v.findViewById(R.id.window_title);

                    // Getting reference to the TextView to set directions snippet
                    TextView windowSnippet = (TextView) v.findViewById(R.id.window_snippet);

                    // Setting the title
                    windowTitle.setText(title);
                    windowTitle.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

                    // Setting the directions snippet
                    windowSnippet.setText("Directions: " + snippet);

                    // Returning the view containing InfoWindow contents
                    return v;
                }
            });

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                // The Map is verified. It is now safe to manipulate the map.
            }
        }
    }
    // Adds bathrooms from json query
    double[] distances;
    int closestLoc = -1;
    LatLng[] locations;
    String[] names;
    // Array that keeps track of the locations that have already been cycled through with the next button -- 99 is max query of locations right now
    int currentLoc[];
    // Array for the back button -- No longer used?, could probably combine current and last, but having two separate arrays was simpler for the time
    int lastLoc[];
    int location_count = 0;

    // Handles both the address search in the action bar and the nearest locations search when gps is on
    public void onSearchResults(List<Bathroom> results) {
        locations = new LatLng[results.size()];
        names = new String[results.size()];
        numLocations = results.size();
        currentLoc = new int[numLocations];
        lastLoc = new int[numLocations];
        if (onSearchAction) {
            // clear map markers before new displaying additional search results
            mMap.clear();
            onSearchAction = false;
        }

        for (int i = 0; i < numLocations; i++)
        {
            Bathroom bathroom = results.get(i);
            LatLng temp = bathroom.getLocation();
            String name = bathroom.getName();
           
            int score = bathroom.getScore();
            //String comment = bathroom.getComments();
            // Adds bathroom markers, blue for accessible, red for not
            if (bathroom.isAccessible() == true)
            {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(temp.latitude, temp.longitude))
                        .title(name)
                        .snippet(bathroom.getDirections())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
            else
            {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(temp.latitude,temp.longitude))
                        .title(name)
                        .snippet(bathroom.getDirections())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }

            locations[i] = temp;
            names[i] = name;
        }
        // If no location, navigate to first marker that was found on search
        if (mCurrentLocation == null) {
            if (numLocations != 0) {
                Toast.makeText(this,R.string.restrooms_found,
                        Toast.LENGTH_SHORT).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locations[0], 13));
            }
            else {
                Toast.makeText(this, R.string.no_nearby_locations,
                        Toast.LENGTH_SHORT).show();
            }
        }

        // New marker onclicklistener to navigate to selected marker
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            // On marker click
            public boolean onMarkerClick(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 400, null);
                marker.showInfoWindow();
                //mMap.getUiSettings().setMapToolbarEnabled(true);
                if (mCurrentLocation != null) {
                    navigateToMarker(marker);
                }
                else {
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                            mGoogleApiClient);
                    if (mLastLocation != null) {
                        navigateToMarker(marker);
                    }
                }
                return true;
            }
        });

        // Find closest location
        double posLat;
        double posLng;
        double myLat = 0;
        double myLng = 0;
        // Checks initial boolean value because otherwise after coming back from text directions
        // the closest value is reset to it's initial value, not what was selected to navigate to
        if (mCurrentLocation != null && numLocations > 0 && initial == true) {
            myLat = mCurrentLocation.getLatitude();
            myLng = mCurrentLocation.getLongitude();

            // Defined before now
            // int closestLoc = -1;
            distances = new double[numLocations];
            // For loop to find the nearest bathroom
            for(int i=0; i < numLocations; i++){
                // Gets i'th array locations latlng
                posLat = locations[i].latitude;
                posLng = locations[i].longitude;

                // Haversine formula which computes shortest distance between two points on a sphere
                distances[i] = Haversine.formula(myLat, myLng, posLat, posLng);

                if ( closestLoc == -1 || distances[i] < distances[closestLoc] ) {
                    closestLoc = i;
                }
            }
            currentLoc[closestLoc] = closestLoc;
            lastLoc[location_count] = closestLoc;
        }
        // Make sure end location doesn't change
        if (mCurrentLocation != null && initial == true) {
            if (numLocations > 0) {
                end = locations[closestLoc];
                getSupportActionBar().setTitle(names[closestLoc]);

                Routing routing = new Routing(Routing.TravelMode.WALKING);
                routing.registerListener(this);
                routing.execute(start, end);
                initial = false;
            }
            else {
                Toast.makeText(this,R.string.no_nearby_locations_initial,
                        Toast.LENGTH_LONG).show();
            }
        }
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
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Navigates to Maps Marker when selected
    public void navigateToMarker(Marker marker) {
        if (mCurrentLocation != null) {
            end = marker.getPosition();
            getSupportActionBar().setTitle(marker.getTitle());
            mLocationTitle = marker.getTitle();

            Routing routing = new Routing(Routing.TravelMode.WALKING);
            routing.registerListener(this);
            routing.execute(start, end);
        }
        else if (mLastLocation != null) {
            end = marker.getPosition();
            getSupportActionBar().setTitle(marker.getTitle());

            double myLat = mLastLocation.getLatitude();
            double myLng = mLastLocation.getLongitude();
            start = new LatLng(myLat,myLng);
            mLocationTitle = marker.getTitle();

            Routing routing = new Routing(Routing.TravelMode.WALKING);
            routing.registerListener(this);
            routing.execute(start, end);
        }
    }
    //TODO Possibly fix navigation drawer to be a smoother switch between the map and fragments -- also redundant onSectionAttached
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // Update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment mFragment = null;
        switch(position) {
            default:
            case 0:
                break;
            case 1:
                mTitle = getString(R.string.title_section2);
                mFragment = new AddBathroomFragment();
                break;
            case 2:
                mTitle = getString(R.string.title_section3);
                mFragment = new FeedbackFormFragment();
                break;
        }
        if (mFragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mFragment)
                    .commit();
        }
        // Added this part because Google Map is not a fragment that can be switched to.
        else {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                    .commit();
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            // Associate searchable configuration with the SearchView
            SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final ArrayAdapterSearchView searchView =
                    (ArrayAdapterSearchView) menu.findItem(R.id.action_search).getActionView();

            searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Bathroom bathroom = searchView.getItemFromAdapter(position);
                    List<Bathroom> bathroomList = new ArrayList<>();
                    bathroomList.add(bathroom);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(bathroom.getLocation()), 400, null);
                    onSearchResults(bathroomList);
                }
            });
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getComponentName()));
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.action_directions:
                Intent intent = new Intent(MainActivity.this, TextDirectionsActivity.class);
                //passes in current location to TextDirectionsActivity
                if (mCurrentLocation != null && end != null) {
                    double tmpLat = mCurrentLocation.getLatitude();
                    double tmpLng = mCurrentLocation.getLongitude();
                    // string manipulation here to get in the right format for API call
                    String start = Double.toString(tmpLat) + " " + Double.toString(tmpLng);
                    //LatLng object
                    tmpLat = end.latitude;
                    tmpLng = end.longitude;
                    String end = Double.toString(tmpLat) + " " + Double.toString(tmpLng);

                    Bundle extras = new Bundle();
                    extras.putString("START_LOC", start);
                    extras.putString("END_LOC", end);
                    extras.putString("TITLE", mLocationTitle);
                    intent.putExtras(extras);
                    startActivity(intent);
                    return true;
                }
                else if (mCurrentLocation == null) {
                    Toast.makeText(this, R.string.location_not_enabled, Toast.LENGTH_SHORT).show();
                    return false;
                }
                else if (end == null) {
                    Toast.makeText(this, R.string.no_nearby_locations, Toast.LENGTH_SHORT).show();
                    return false;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
