package org.refugerestrooms.views;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.refugerestrooms.R;
import org.refugerestrooms.application.RefugeRestroomApplication;
import org.refugerestrooms.database.SaveBathroomPropertyHandler;
import org.refugerestrooms.database.model.BathroomEntityDao;
import org.refugerestrooms.database.model.DaoSession;
import org.refugerestrooms.database.model.DatabaseEntityConverter;
import org.refugerestrooms.models.Bathroom;
import org.refugerestrooms.models.Haversine;
import org.refugerestrooms.servers.Server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener,
        RoutingListener,
        Server.ServerListener {

    private FloatingActionButton mFab;
    private Toolbar mToolbar;
    private MapView mMapView;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private View bottomSheet;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private boolean initial = true;
    private boolean searchPerformed;

    private Location mCurrentLocation;
    private Location mLastLocation;

    private LatLng currentPosition;
    private boolean mUpdatesRequested;
    private boolean mInProgress;
    public boolean doNotDisplayDialog;
    public boolean onSearchAction;
    protected LatLng start;
    protected LatLng end;
    // temp lat/lng for setting up initial map
    private static final LatLng COFFMAN = new LatLng(44.972905, -93.235613);

    private int numLocations;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

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
    // Store the current activity recognition client
    //private ActivityRecognitionClient mActivityRecognitionClient;

	/*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */

    // Check for Google Play Services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 123;

    private String mLocationTitle;

    private String query;

    private Polyline poly1;
    private Polyline poly2;

    private Server mServer;

    // Adds bathrooms from json query
    private double[] distances;
    private int closestLoc = -1;
    private LatLng[] locations;
    private String[] names;
    // Array that keeps track of the locations that have already been cycled through with the next button -- 99 is max query of locations right now
    private int[] currentLoc;
    // Array for the back button -- No longer used?, could probably combine current and last, but having two separate arrays was simpler for the time
    private int[] lastLoc;
    private int location_count;
    // Create hashmap to store bathrooms (Key = LatLng, Value = Bathroom)
    private final Map<LatLng, Bathroom> allBathroomsMap = new HashMap<>();

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                /*
                 * If the result code is Activity.RESULT_OK, try
                 * to connect again
                 */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        /*
                         * Try the request again
                         */
                        break;
                    default:
                        break;
                }
            default:
                break;
        }
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else { // Google Play services was not available for some reason
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode, this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(errorDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }
            return false;
        }
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        if (servicesConnected()) {
            // Display the connection status
            Snackbar.make(mFab, R.string.connected, Snackbar.LENGTH_SHORT).show();
            // If already requested, start periodic updates
            // 3rd parameter just (this)?
            if (mUpdatesRequested) {
                LocationServices.FusedLocationApi.requestLocationUpdates(
                        mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
            }
            //TODO debug this part when no wifi/gps/mobile

            // Get the current location and move camera to it
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            /*******************************************************************
             * API call to Refuge Restrooms here
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
            } else {
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
             **************************************************************/

            if (mCurrentLocation != null) {
                double myLat = mCurrentLocation.getLatitude();
                double myLng = mCurrentLocation.getLongitude();
                currentPosition = new LatLng(myLat, myLng);
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
	    Snackbar.make(mFab, "Disconnected. Please re-connect.",
	            Snackbar.LENGTH_SHORT).show();
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

    // Launches the detailed info view from InfoViewFragment
    private void launchDetails(Bathroom bathroom) {
        Bundle bundle = new Bundle();
        bundle.putString(InfoViewFragment.EXTRA_BATHROOM, bathroom.toJson());
        InfoViewFragment infoView = new InfoViewFragment();
        infoView.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, infoView)
                .addToBackStack("infoView")
                .commit();
        mFab.setVisibility(View.INVISIBLE);
    }

    // Updates the bottom sheet with the latest selected item
    private void setBottomSheet(final Bathroom bathroom) {
        if (bathroom == null) {
            bottomSheet.setVisibility(View.GONE);
            return;
        }
        bottomSheet.setVisibility(View.VISIBLE);
        TextView title = (TextView) findViewById(R.id.text_title);
        TextView address = (TextView) findViewById(R.id.text_address);
        TextView comments = (TextView) findViewById(R.id.text_comments);
        title.setText(bathroom.getNameFormatted());
        address.setText(bathroom.getAddressFormatted());
        comments.setText(Html.fromHtml(bathroom.getCommentsFormatted()));
        View specsView = findViewById(R.id.specs);
        BathroomSpecsViewUpdater.update(specsView, bathroom, this);
        findViewById(R.id.button_maps).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double lat = bathroom.getmLatitude();
                double lon = bathroom.getmLongitude();
                // Names need to be escaped, so a space should be replaced by either a + or by %20
                String addressEscaped = bathroom.getAddress().replace(' ', '+');
                String uri = "geo:" + lat + "," + lon + "?q=" + addressEscaped;
                Uri intentUri = Uri.parse(uri);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, intentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
    }

    private void expandBottomSheet() {
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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
        }
    }

    void showErrorDialog(int code) {
        GooglePlayServicesUtil.getErrorDialog(code, this,
                CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.no_marker_selected, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        bottomSheet = findViewById(R.id.bottom_info_sheet);

        // For search results
        handleIntent(getIntent());

        // TODO
        // Checks if gps is enabled, kicks out message to turn on if not.

        if (servicesConnected()) {
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

            // Start with updates turned off
            mUpdatesRequested = false;

            setToolbarTitle("Refuge Restrooms");
        }
    }

    private void setToolbarTitle(String title) {
        mToolbar.setTitle(title);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    // For search activity in action bar
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
            // Use the query to search
            Server mServer = new Server(this);
            // Boolean to prevent "gps not enabled" dialog box from re-showing on search
            doNotDisplayDialog = true;
            onSearchAction = true;
            searchPerformed = true;
            mServer.performSearch(query, false);
        }
    }

    @Override
    public void onRoutingFailure() {
        // The Routing request failed
    }

    @Override
    public void onRoutingStart() {
        // The Routing Request starts
    }

    @Override
    public void onRoutingSuccess(PolylineOptions mPolyOptions, Route route) {
        //removes polyline on update to create new one
        if (poly1 != null) {
            poly1.remove();
        }
        if (poly2 != null) {
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

    @Override
    protected void onPause() {
        // Save the current setting for updates
        if (mEditor != null) {
            mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
            mEditor.commit();
        }
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        drawer.removeDrawerListener(toggle);
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        mMapView.onResume();
        if (mPrefs != null) {
            if (mPrefs.contains("KEY_UPDATES_ON")) {
                mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);

                // Otherwise, turn off location updates
            } else {
                if (mEditor != null) {
                    mEditor.putBoolean("KEY_UPDATES_ON", false);
                    mEditor.commit();
                }
            }
        }
    }

    /*
     * Called when the Activity is no longer visible.
     */
    @Override
    protected void onStop() {
        // If the client is connected
        if (mGoogleApiClient != null) {
            /*
             * After disconnect() is called, the client is
             * considered "dead".
             */
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        if (location != null) {
            String msg = "Updated Location: " +
                    Double.toString(location.getLatitude()) + "," +
                    Double.toString(location.getLongitude());
            Snackbar.make(mFab, msg, Snackbar.LENGTH_SHORT).show();
            mCurrentLocation = location;
        }
    }

    /*
     * Request activity recognition updates based on the current detection interval.
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
        } else {
            /*
             * A request is already underway. Can handle
             * this situation by disconnecting the client,
             * re-setting the flag, and then re-trying the
             * request.
             */
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
                    mGoogleApiClient.connect();
                    mMap.setMyLocationEnabled(true);
                } else {
                    // TODO something
                }
            default:
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(COFFMAN, 15));
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setInfoWindowAdapter(new BathroomInfoWindow(this));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        } else {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            mGoogleApiClient.connect();
            mMap.setMyLocationEnabled(true);
        }
    }

    // Handles both the address search in the action bar and the nearest locations search when gps is on
    public void onSearchResults(final List<Bathroom> results) {
        loadBathrooms(results);
    }

    private void loadBathrooms(List<Bathroom> results) {
        numLocations = results.size();
        locations = new LatLng[numLocations];
        names = new String[numLocations];
        currentLoc = new int[numLocations];
        lastLoc = new int[numLocations];
        if (onSearchAction) {
            // clear map markers before new displaying additional search results
            mMap.clear();
            onSearchAction = false;
        }

        for (int i = 0; i < numLocations; i++) {
            Bathroom bathroom = results.get(i);
            DaoSession daoSession = RefugeRestroomApplication.getInstance().getDaoSession();
            SaveBathroomPropertyHandler.saveProperty(daoSession, bathroom);

            LatLng temp = bathroom.getLocation();
            String name = bathroom.getName();

            // Used to encode additional information in the Marker object for displaying the info window
            int isAccessible = bathroom.isAccessible() ? 1 : 0;
            int isUnisex = bathroom.isUnisex() ? 1 : 0;
            float hue = (isAccessible == 1) ? BitmapDescriptorFactory.HUE_AZURE : BitmapDescriptorFactory.HUE_RED;

            // Adds bathroom markers, blue for accessible, red for not
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(temp.latitude, temp.longitude))
                    .title(name)
                    .snippet(bathroom.getAddress() + "*" + isAccessible + isUnisex)
                    .icon(BitmapDescriptorFactory.defaultMarker(hue)));
            // Put bathrooms in hashmap for use later in info window
            allBathroomsMap.put(bathroom.getLocation(), bathroom);

            locations[i] = temp;
            names[i] = name;
        }
        // If no location, navigate to first marker that was found on search
        if (mCurrentLocation == null) {
            if (numLocations != 0) {
                Snackbar.make(mFab, R.string.restrooms_found, Snackbar.LENGTH_SHORT).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locations[0], 13));
            } else {
                // Concatenates no_search_locations from strings.xml with search term
                if (query != null) {
                    String text = String.format(getResources().getString(R.string.no_search_locations), query);
                    Snackbar.make(mFab, text, Snackbar.LENGTH_SHORT).show();
                }
                // No query here indicates that user selected Recent Bathrooms and there were none in Dao
                else {
                    Snackbar.make(mFab, R.string.no_recent_locations, Snackbar.LENGTH_SHORT).show();

                }
            }
        }
        // Create info Button and set initial onclicklistener to return snackbar message
        // (before map pin is selected)
        mFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Snackbar.make(v, R.string.no_marker_selected, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // New marker onclicklistener to navigate to selected marker
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            // On marker click
            public boolean onMarkerClick(Marker marker) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()), 400, null);
                marker.showInfoWindow();

                final LatLng markerLatLng = marker.getPosition();
                setBottomSheet(allBathroomsMap.get(markerLatLng));
                // Set onclicklistener for info button -- override snackbar message
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*
                        Bathroom bathroom;
                        // Get bathroom from hashmap using marker's location
                        bathroom = allBathroomsMap.get(markerLatLng);
                        if (bathroom != null) {
                            launchDetails(bathroom);
                        }
                        */
                        launchTextDirections();
                    }
                });
                //mMap.getUiSettings().setMapToolbarEnabled(true);
                if (mCurrentLocation != null) {
                    navigateToMarker(marker);
                }
                // was causing java.lang.IllegalArgumentException: GoogleApiClient parameter is required.
//                else {
//                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
//                            mGoogleApiClient);
//                    if (mLastLocation != null) {
//                        navigateToMarker(marker);
//                    }
//                }
                return true;
            }
        });

        // On info window click
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Bathroom bathroom;
                // Get bathroom from hashmap using marker's location
                bathroom = allBathroomsMap.get(marker.getPosition());
                if (bathroom != null) {
                    //launchDetails(bathroom);
                    setBottomSheet(bathroom);
                    expandBottomSheet();
                }
            }
        });

        // Find closest location
        double posLat;
        double posLng;
        double myLat;
        double myLng;
        // Checks initial boolean value because otherwise after coming back from text directions
        // the closest value is reset to it's initial value, not what was selected to navigate to
        if (mCurrentLocation != null && numLocations > 0 && initial) {
            myLat = mCurrentLocation.getLatitude();
            myLng = mCurrentLocation.getLongitude();

            // Defined before now
            // int closestLoc = -1;
            distances = new double[numLocations];
            // For loop to find the nearest bathroom
            for (int i = 0; i < numLocations; i++) {
                // Gets i'th array locations latlng
                posLat = locations[i].latitude;
                posLng = locations[i].longitude;

                // Haversine formula which computes shortest distance between two points on a sphere
                distances[i] = Haversine.formula(myLat, myLng, posLat, posLng);

                if (closestLoc == -1 || distances[i] < distances[closestLoc]) {
                    closestLoc = i;
                }
            }
            currentLoc[closestLoc] = closestLoc;
            lastLoc[location_count] = closestLoc;
        }
        // Make sure end location doesn't change
        if (mCurrentLocation != null && initial) {
            if (numLocations > 0) {
                end = locations[closestLoc];
                setToolbarTitle(names[closestLoc]);
                mLocationTitle = names[closestLoc];
                final LatLng defaultLocation = new LatLng(end.latitude, end.longitude);
                setBottomSheet(allBathroomsMap.get(defaultLocation));
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*
                        Bathroom bathroom;
                        // Get bathroom from hashmap using marker's location
                        bathroom = allBathroomsMap.get(defaultLocation);
                        if (bathroom != null) {
                            launchDetails(bathroom);
                        }
                        */
                        launchTextDirections();
                    }
                });

                Routing routing = new Routing(Routing.TravelMode.WALKING);
                routing.registerListener(this);
                routing.execute(start, end);
                initial = false;
            } else {
                // Check to see if a bathroom wasn't found because of a search, or from gps, and
                // display appropriate message
                int textRes = searchPerformed
                        ? R.string.no_search_locations_initial
                        : R.string.no_nearby_locations_initial;
                Snackbar.make(mFab, textRes, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    private List loadSavedBathrooms() {
        DaoSession daoSession = RefugeRestroomApplication.getInstance().getDaoSession();
        BathroomEntityDao leaseDao = daoSession.getBathroomEntityDao();
        // Loads the last 150 bathrooms added to the database
        List restroomsList = leaseDao.queryBuilder().orderDesc(BathroomEntityDao.Properties.Timestamp).limit(150).list();
        //List restroomsList = leaseDao.loadAll();
        return restroomsList;
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
                Snackbar.make(mFab, errorMessage, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    // Navigates to Maps Marker when selected
    public void navigateToMarker(Marker marker) {
        if (mCurrentLocation != null) {
            end = marker.getPosition();
            setToolbarTitle(marker.getTitle());
            mLocationTitle = marker.getTitle();

            Routing routing = new Routing(Routing.TravelMode.WALKING);
            routing.registerListener(this);
            routing.execute(start, end);
        }
        // was causing java.lang.IllegalArgumentException: GoogleApiClient parameter is required.
//        else if (mLastLocation != null) {
//            end = marker.getPosition();
//            setActionBarTitle(marker.getTitle());
//
//            double myLat = mLastLocation.getLatitude();
//            double myLng = mLastLocation.getLongitude();
//            start = new LatLng(myLat,myLng);
//            mLocationTitle = marker.getTitle();
//
//            Routing routing = new Routing(Routing.TravelMode.WALKING);
//            routing.registerListener(this);
//            routing.execute(start, end);
//        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }

    private boolean launchTextDirections() {
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
        } else if (mCurrentLocation == null) {
            Snackbar.make(mFab, R.string.location_not_enabled, Snackbar.LENGTH_SHORT).show();
            return false;
        } else {
            Snackbar.make(mFab, R.string.no_nearby_locations, Snackbar.LENGTH_SHORT).show();
            return false;
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;
        String title = null;
        String fragmentTitle = null;

        if (id == R.id.nav_map) {
            title = getString(R.string.map_title_section);
            fragment = new MapFragment();
            fragmentTitle = "maps";
            mFab.setVisibility(View.VISIBLE);
            bottomSheet.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_bathrooms) {
            title = getString(R.string.saved_bathrooms);
            List bathroomsList = loadSavedBathrooms();
            DatabaseEntityConverter dataEntityConv = new DatabaseEntityConverter();
            List<Bathroom> bathrooms = dataEntityConv.convertBathroomEntity(bathroomsList);
            loadBathrooms(bathrooms);
            mFab.setVisibility(View.VISIBLE);
            bottomSheet.setVisibility(View.VISIBLE);
            onSearchAction = true;
        } else if (id == R.id.nav_add) {
            title = getString(R.string.add_title_section);
            fragment = new AddBathroomFragment();
            fragmentTitle = "addBathroom";
            mFab.setVisibility(View.INVISIBLE);
            bottomSheet.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_feedback) {
            title = getString(R.string.feedback_title_section);
            fragment = new FeedbackFormFragment();
            fragmentTitle = "feedback";
            mFab.setVisibility(View.INVISIBLE);
            bottomSheet.setVisibility(View.INVISIBLE);
        }

        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(fragmentTitle)
                    .commit();
            setToolbarTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}