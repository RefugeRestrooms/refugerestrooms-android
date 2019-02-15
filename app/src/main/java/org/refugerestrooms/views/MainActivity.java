package org.refugerestrooms.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.refugerestrooms.R;
import org.refugerestrooms.application.RefugeRestroomApplication;
import org.refugerestrooms.database.SaveBathroomPropertyHandler;
import org.refugerestrooms.database.model.BathroomEntity;
import org.refugerestrooms.database.model.BathroomEntityDao;
import org.refugerestrooms.database.model.DaoSession;
import org.refugerestrooms.database.model.DatabaseEntityConverter;
import org.refugerestrooms.models.Bathroom;
import org.refugerestrooms.models.Haversine;
import org.refugerestrooms.servers.Server;
import org.refugerestrooms.services.GeocodeAddressIntentService;

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
        Server.ServerListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private static final String CURRENT_LOCATION_KEY = "current-location";

    private FloatingActionButton mFab;
    private Toolbar mToolbar;
    private MapView mMapView;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationRequest mSingleLocationRequest;
    private View bottomSheet;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private boolean initial = true;

    private Location mCurrentLocation;
    private LocationCallback mLocationCallback;

    private LatLng mCurrentPosition;
    private boolean mUpdatesRequested;
    public boolean doNotDisplayDialog;
    protected LatLng mStart;
    protected LatLng mEnd;
    private ResultReceiver mResultReceiver;
    // temp lat/lng for setting up initial map
    private static final LatLng COFFMAN = new LatLng(44.972905, -93.235613);

    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

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

    // Store the current activity recognition client
    //private ActivityRecognitionClient mActivityRecognitionClient;

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */

    // Check for Google Play Services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 123;

    // Check for location settings
    public static final int LOCATION_SETTINGS_REQUEST = 10540;

    private String query;

    private Server mServer;

    // Adds bathrooms from json query
    private int closestLoc = -1;
    // Create hashmap to store bathrooms (Key = LatLng, Value = Bathroom)
    private final Map<LatLng, Bathroom> allBathroomsMap = new HashMap<>();

    private Fragment addBathroomFragment;
    private FragmentManager fragmentManager;

    private Button mSearchHereButton;

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
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /*
     * Handle results returned to the FragmentActivity
     * by Google Play services and Location services
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
                // Covers a request to turn on the location service
            case LOCATION_SETTINGS_REQUEST:
                // If the result is okay, location should now be enabled
                Log.d("RefugeRestrooms", "Location Settings Request Code");
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.d("RefugeRestrooms", "Activity Result Okay");
                        // Create the callback for the location check
                        LocationCallback oneTimeLocationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                // Updates the current location and position
                                mCurrentLocation = locationResult.getLastLocation();
                                double tmpLat = mCurrentLocation.getLatitude();
                                double tmpLng = mCurrentLocation.getLongitude();
                                String curLatLng = "lat=" + Double.toString(tmpLat) + "&lng=" + Double.toString(tmpLng);
                                mCurrentPosition = new LatLng(tmpLat, tmpLng);
                                // Move the camera to the new position
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 15));
                                onLocationChanged(mCurrentLocation);
                                // Performs a search on that location
                                mServer.performSearch(curLatLng, true);
                                mStart = mCurrentPosition;
                                mSearchHereButton.setVisibility(View.INVISIBLE);
                            }
                        };
                        // Request a single location for the callback
                        getSingleLocation(oneTimeLocationCallback);
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user declined to turn their location on,
                        // nothing else to do
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
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else { // Google Play services was not available for some reason
            // Get the error dialog from Google Play services
            Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(
                    this, resultCode, CONNECTION_FAILURE_RESOLUTION_REQUEST);

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
     * request the current location or mStart periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        //TODO break up this method a bit, new Fused version takes more lines of code
        if (servicesConnected()) {
            // Display the connection status
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Snackbar.make(mFab, R.string.connected, Snackbar.LENGTH_SHORT).show();
                }
            });

            // If already requested, mStart periodic updates
            // 3rd parameter just (this)?
            if (mUpdatesRequested) {
                // Check if have access to location already, if not, prompt
                startLocationUpdates();
            }
            //TODO debug this part when no wifi/gps/mobile

            // Create a success listener that will trigger when the last location is found.
            // Then send the listener to be added to the mFusedLocationClient via getLastLocation()
            OnSuccessListener<Location> onSuccessListener = new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Gets bathroom data from RefugeRestrooms.org
                    // (20 closest entries -- defined in Server.java)
                    mCurrentLocation = location;
                    String curLatLng;
                    if (mCurrentLocation != null) {
                        double tmpLat = mCurrentLocation.getLatitude();
                        double tmpLng = mCurrentLocation.getLongitude();
                        mCurrentPosition = new LatLng(tmpLat, tmpLng);
                        mStart = mCurrentPosition;
                        onLocationChanged(mCurrentLocation);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 15));

                        // String manipulation here to get in the right format for API call
                        curLatLng = "lat=" + Double.toString(tmpLat) + "&lng=" + Double.toString(tmpLng);
                        mServer.performSearch(curLatLng, true);
                    }
                }
            };
            getLastLocation(onSuccessListener);
        }
        mMap.setOnMyLocationButtonClickListener(new GoogleMap
                .OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                // Check for location permissions, and prompt if unavailable
                OnSuccessListener<Location> successListener = new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        mCurrentLocation = location;
                        if (mCurrentLocation != null) {
                            // Assemble new position
                            double tmpLat = mCurrentLocation.getLatitude();
                            double tmpLng = mCurrentLocation.getLongitude();
                            String curLatLng = "lat=" + Double.toString(tmpLat) + "&lng=" + Double.toString(tmpLng);
                            mCurrentPosition = new LatLng(tmpLat, tmpLng);
                            // Move camera to new position
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 15));
                            onLocationChanged(mCurrentLocation);
                            // Launch bathroom search for new position
                            mServer.performSearch(curLatLng, true);
                            mStart = mCurrentPosition;
                            mSearchHereButton.setVisibility(View.INVISIBLE);
                        }
                    }
                };
                // Send the listener to be attached to the FusedLocationClient
                getLastLocation(successListener);
                return false;
            }
        });
    }

    // Updates the bottom sheet with the latest selected item
    private void setBottomSheet(final Bathroom bathroom) {
        if (bathroom == null) {
            bottomSheet.setVisibility(View.GONE);
            return;
        }
        bottomSheet.setVisibility(View.VISIBLE);
        TextView title = findViewById(R.id.text_title);
        TextView address = findViewById(R.id.text_address);
        TextView comments = findViewById(R.id.text_comments);
        title.setText(bathroom.getNameFormatted());
        address.setText(bathroom.getAddressFormatted());
        comments.setText(Html.fromHtml(bathroom.getCommentsFormatted()));
        View specsView = findViewById(R.id.specs);
        BathroomSpecsViewUpdater.update(specsView, bathroom, this);

        // Change text padding to center in the bottom sheet peek.
        int lineCount = title.getLineCount();
        if (lineCount == 1) {
            // Single displayed line padding
            title.setPadding(title.getPaddingLeft(),
                    getResources().getDimensionPixelOffset(R.dimen.one_line_padding),
                    title.getPaddingRight(),
                    title.getPaddingBottom());
        } else if (lineCount == 2) {
            // Two displayed line padding
            title.setPadding(title.getPaddingLeft(),
                    getResources().getDimensionPixelOffset(R.dimen.two_line_padding),
                    title.getPaddingRight(),
                    title.getPaddingBottom());
        } else {
            // Else 0 top padding
            title.setPadding(title.getPaddingLeft(),
                    getResources().getDimensionPixelOffset(R.dimen.zero_padding),
                    title.getPaddingRight(),
                    title.getPaddingBottom());
        }

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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * mStart a Google Play services activity that can resolve
         * error.
         */
        // Turn off the request flag
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
        GoogleApiAvailability.getInstance().getErrorDialog(this, code,
                CONNECTION_FAILURE_RESOLUTION_REQUEST).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mCurrentLocation != null) {
            outState.putParcelable(CURRENT_LOCATION_KEY, mCurrentLocation);
        } else {
            outState.putParcelable(CURRENT_LOCATION_KEY, null);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mCurrentLocation = savedInstanceState.getParcelable(CURRENT_LOCATION_KEY);
        }

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, R.string.no_marker_selected, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mServer = new Server(MainActivity.this);

        mMapView = findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Create an instance of the bathroom fragment to pre-load the website into the cache.
        // Swap this instance in later when selected.
        addBathroomFragment = new AddBathroomFragment();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.attach(addBathroomFragment);
        fragmentTransaction.commit();

        bottomSheet = findViewById(R.id.bottom_info_sheet);

        mResultReceiver = new AddressResultReceiver(null);
        // For search results
        handleIntent(getIntent());

        // Create single location request for acquiring only one non-null location.
        mSingleLocationRequest = LocationRequest.create();
        // Location Accuracy
        mSingleLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mSingleLocationRequest.setInterval(UPDATE_INTERVAL);
        mSingleLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        // Set the number of updates requested to 1
        mSingleLocationRequest.setNumUpdates(1);

        // Create the LocationRequest object for regular location updates
        mLocationRequest = LocationRequest.create();
        // Location Accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // TODO
        // Checks if gps is enabled, kicks out message to turn on if not.

        if (servicesConnected()) {
            // Open the shared preferences
            mPrefs = getSharedPreferences("SharedPreferences",
                    Context.MODE_PRIVATE);
            // Get a SharedPreferences editor
            mEditor = mPrefs.edit();
            mEditor.apply();

            // Initialize the location client
            mFusedLocationClient = LocationServices
                    .getFusedLocationProviderClient(this);

            // Is called every 20 seconds or so while location updates are requested
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    // Updates the current location and position
                    mCurrentLocation = locationResult.getLastLocation();
                    double tmpLat = mCurrentLocation.getLatitude();
                    double tmpLng = mCurrentLocation.getLongitude();
                    String curLatLng = "lat=" + Double.toString(tmpLat) + "&lng=" + Double.toString(tmpLng);
                    mCurrentPosition = new LatLng(tmpLat, tmpLng);
                    //TODO Add a setting to enable or disable camera tracking of current position.
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 15));
                    onLocationChanged(mCurrentLocation);
                    // Performs a search on that location
                    mServer.performSearch(curLatLng, true);
                    mStart = mCurrentPosition;
                }
            };

            // Access stored location if exists
            double lastLocationLat = Double.longBitsToDouble(mPrefs.getLong("current_lat", Double.doubleToLongBits(0.0d)));
            double lastLocationLon = Double.longBitsToDouble(mPrefs.getLong("current_lon", Double.doubleToLongBits(0.0d)));
            // If a stored location is found, set that as the application default
            if (lastLocationLat != 0.0d || lastLocationLon != 0.0d) {
                mCurrentLocation = new Location("");
                mCurrentLocation.setLatitude(lastLocationLat);
                mCurrentLocation.setLongitude(lastLocationLon);
                mCurrentPosition = new LatLng(lastLocationLat, lastLocationLon);
            }

            // Start with updates turned off
            mUpdatesRequested = false;

            setToolbarTitle("Refuge Restrooms");

            mSearchHereButton = findViewById(R.id.search_here_button);
            mSearchHereButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentPosition = mMap.getCameraPosition().target;
                    mCurrentLocation = new Location("");
                    mCurrentLocation.setLatitude(mCurrentPosition.latitude);
                    mCurrentLocation.setLongitude(mCurrentPosition.longitude);

                    String curLatLng = "lat=" + Double.toString(mCurrentPosition.latitude) +
                            "&lng=" + Double.toString(mCurrentPosition.longitude);

                    onLocationChanged(mCurrentLocation);
                    // Performs a search on that location
                    mServer.performSearch(curLatLng, true);
                    mStart = mCurrentPosition;
                    mSearchHereButton.setVisibility(View.INVISIBLE);
                }
            });
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

            Intent startGeocodeIntent = new Intent(this, GeocodeAddressIntentService.class);
            startGeocodeIntent.putExtra(GeocodeAddressIntentService.RECEIVER, mResultReceiver);
            startGeocodeIntent.putExtra(GeocodeAddressIntentService.LOCATION_NAME_DATA_EXTRA, query);
            startService(startGeocodeIntent);
        }
    }

    // Receiver for address search result
    class AddressResultReceiver extends ResultReceiver {
        private AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, final Bundle resultData) {
            if (resultCode == GeocodeAddressIntentService.SUCCESS_RESULT) {
                final Address address = resultData.getParcelable(GeocodeAddressIntentService.RESULT_ADDRESS);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        assert address != null;
                        // Get lat and long of searched address
                        double latitude = address.getLatitude();
                        double longitude = address.getLongitude();
                        Log.d(TAG, latitude + " " + longitude);

                        LatLng addressPosition = new LatLng(latitude, longitude);

                        // Set current location and route starting point to searched address
                        Location addressLocation = new Location("");
                        addressLocation.setLatitude(latitude);
                        addressLocation.setLongitude(longitude);
                        onLocationChanged(addressLocation);
                        mStart = addressPosition;

                        // Move map to searched address
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(addressPosition));

                        // Use the latitude and longitude to search for restrooms
                        // Boolean to prevent "gps not enabled" dialog box from re-showing on search
                        doNotDisplayDialog = true;
                        String searchTerm = Server.getSearchTermFromLatLng(latitude, longitude);
                        Log.e(TAG, searchTerm);
                        mServer.performSearch(searchTerm, true);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Location not found.");
                        Toast.makeText(MainActivity.this, R.string.location_not_found, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    /**
     * Checks for location permissions, then for location services being enabled on the user device.
     * Attaches a given OnSuccessListener to a getLastLocation() call on the Location Client.
     * After the last known location is found, the success listener is executed.
     *
     * This method is intended to centralize getLastLocation calls, and add permission and service
     * checks easily and with minimal code repetition across the Activity.
     *
     * Warning: getLastLocation can return a null location if called soon after
     * location services are turned on.
     *
     * For a guarenteed location, call getSingleLocation() and pass a callback function.
     *
     * @param onSuccessListener the code to execute with the location when found
     */
    private void getLastLocation(OnSuccessListener<Location> onSuccessListener) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Ask for permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }
        // Check that location settings are enabled, if not then prompt
        checkLocationSettings();
        // Retrieve last location and attach the given success listener to the result.
        mFusedLocationClient.getLastLocation().addOnSuccessListener(onSuccessListener);
    }

    /**
     * Retrieves a single non-null location and attaches a given callback to that location.
     * Use this when a single non-null location is needed, or when location services
     * have just started (i.e. just after a settings request).
     *
     * @param locationCallback the code to execute when a non-null location is found
     */
    private void getSingleLocation(LocationCallback locationCallback) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Ask for permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }
        // Check that location settings are enabled, if not then prompt
        checkLocationSettings();
        mFusedLocationClient.requestLocationUpdates(mSingleLocationRequest,
                locationCallback,
                null);

    }

    /**
     * Checks for location permissions, then for location services being enabled on the user device.
     * Starts a request for location updates based on the request interval defined in mLocationRequest.
     * Executes the code specified in mLocationCallback each time the location is updated.
     *
     * This method is intended to centralize the enabling of regular location updates.
     */
    private void startLocationUpdates() {
        //TODO When options menu is finished, add a toggle for this feature.
        // Check Location permissions
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Ask for permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_ACCESS_FINE_LOCATION);
        }
        // Check location services setting on device
        checkLocationSettings();
        // Create callback using the request intervals
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null);
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onPause() {
        // Save the current setting for updates
        if (mFusedLocationClient != null) {
            stopLocationUpdates();
        }
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
                // Updates are already disabled on pause, turn them back on if requested.
                if (mUpdatesRequested) {
                    if (mFusedLocationClient != null) {
                        startLocationUpdates();
                        Snackbar.make(mFab, "Finding your current location...", Snackbar.LENGTH_SHORT);
                    }
                }
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

    /**
     * Called each time the current location is updated.
     * Saves the current location to shared prefs for offline location defaults.
     * Displays a message stating that the location has changed.
     *
     * @param location A valid new Location to inform the app about
     */
    @Override
    public void onLocationChanged(Location location) {
        // Report to the UI that the location was updated
        if (location != null) {
            String msg = "Loading new bathrooms...";
            Snackbar.make(mFab, msg, Snackbar.LENGTH_SHORT).show();
            mCurrentLocation = location;

            if (mEditor != null) {
                // Convert the latitude and longitude into raw long bits for safe storage in
                // shared preferences, remember to turn back into a double when read
                Log.d(TAG, "Saving location");
                mEditor.putLong("current_lat", Double.doubleToRawLongBits(mCurrentLocation.getLatitude()));
                mEditor.putLong("current_lon", Double.doubleToRawLongBits(mCurrentLocation.getLongitude()));
                mEditor.commit();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(LocationServices.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();
                    mGoogleApiClient.connect();
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // Prompt for permissions
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION},
                                MY_PERMISSIONS_ACCESS_FINE_LOCATION);
                    }
                    mMap.setMyLocationEnabled(true);
                } else {
                    // In case they would like to change their mind about permissions
                    mMap.setMyLocationEnabled(true);
                }
            default:
                break;
        }
    }

    /**
     * Checks device location service settings. If disabled, prompts the user to enable them
     * with a dialog box.
     *
     * In the case that Location services are unavailable for other reasons, a prompt is not created.
     */
    private void checkLocationSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build())
                .addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                        try {
                            task.getResult(ApiException.class);
                            // Location settings are satisfied, no need to display the dialogue
                        } catch (ApiException exception) {
                            switch (exception.getStatusCode()) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    // Location settings are not satisfied, but could be fixed
                                    try {
                                        // First make the exception resolvable
                                        ResolvableApiException resolvable =
                                                (ResolvableApiException) exception;
                                        // Show the dialogue
                                        startIntentSenderForResult(resolvable.getResolution().getIntentSender(),
                                                LOCATION_SETTINGS_REQUEST,
                                                null, 0, 0, 0, null);
                                    } catch (IntentSender.SendIntentException | ClassCastException e) {
                                        // Ignorable error
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    // Location settings aren't satisfied, but there's no way
                                    // to change the settings
                                    break;
                            }
                        }
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if (mCurrentPosition == null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(COFFMAN, 15));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 13));
        }

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

    //TODO Add Javadoc
    private void loadBathrooms(List<Bathroom> results) {
        int numLocations;

        numLocations = results.size();
        LatLng[] locations = new LatLng[numLocations];
        String[] names = new String[numLocations];

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
            mMap.addMarker(new MarkerOptions()
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
                        launchNavigation();
                    }
                });

                if (mCurrentLocation != null) {
                    navigateToMarker(marker);
                }

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

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                if (mCurrentPosition.latitude != 0.0d && mCurrentPosition.longitude != 0.0) {
                    mSearchHereButton.setVisibility(View.VISIBLE);
                } else {
                    mCurrentPosition = mMap.getCameraPosition().target;
                    mCurrentLocation.setLatitude(mCurrentPosition.latitude);
                    mCurrentLocation.setLongitude(mCurrentPosition.longitude);
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
            double distances[] = new double[numLocations];
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
        }
        // Make sure mEnd location doesn't change
        if (mCurrentLocation != null && initial) {
            if (numLocations > 0) {
                mEnd = locations[closestLoc];
                setToolbarTitle(names[closestLoc]);
                final LatLng defaultLocation = new LatLng(mEnd.latitude, mEnd.longitude);
                setBottomSheet(allBathroomsMap.get(defaultLocation));
                mFab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchNavigation();
                    }
                });
                initial = false;
            } else {
                Snackbar.make(mFab, R.string.no_search_locations_initial, Snackbar.LENGTH_LONG).show();
            }
        }
    }

    //TODO Add Javadoc
    private List<BathroomEntity> loadSavedBathrooms() {
        DaoSession daoSession = RefugeRestroomApplication.getInstance().getDaoSession();
        BathroomEntityDao leaseDao = daoSession.getBathroomEntityDao();
        // Loads the last 150 bathrooms added to the database
        return leaseDao.queryBuilder().orderDesc(BathroomEntityDao.Properties.Timestamp).limit(150).list();
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
            mEnd = marker.getPosition();
            setToolbarTitle(marker.getTitle());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

    //TODO Add Javadoc
    private void launchNavigation() {
        if (mCurrentLocation != null && mEnd != null) {
            // Walking mode to currently set end location from current GPS location.
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + mEnd.latitude + "," + mEnd.longitude + "&mode=w");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                Snackbar.make(mFab, "Google Maps App not found.", Snackbar.LENGTH_SHORT).show();

                // If Google Maps app can't be launched, instead launch the Android browser.
                Uri browserIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=" +
                        mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude()
                        + "&destination=" + mEnd.latitude + "," + mEnd.longitude
                        + "&travelmode=walking");
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, browserIntentUri);
                startActivity(browserIntent);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        String title = null;
        String fragmentTitle = null;

        if (id == R.id.nav_map) {
            title = getString(R.string.map_title_section);
            fragment = new MapFragment();
            fragmentTitle = "maps";
            mFab.show();
            bottomSheet.setVisibility(View.VISIBLE);
        } else if (id == R.id.nav_bathrooms) {
            Log.d("RefugeRestrooms", "Nav bathrooms");
            title = getString(R.string.saved_bathrooms);
            List<BathroomEntity> bathroomsList = loadSavedBathrooms();
            DatabaseEntityConverter dataEntityConv = new DatabaseEntityConverter();
            List<Bathroom> bathrooms = dataEntityConv.convertBathroomEntity(bathroomsList);
            loadBathrooms(bathrooms);
            mFab.show();
            bottomSheet.setVisibility(View.VISIBLE);
            //Toast.makeText(this, "Loading recent bathrooms...", Toast.LENGTH_SHORT).show();
            Snackbar.make(mFab, "Loading recent bathrooms...", Snackbar.LENGTH_SHORT).show();
        } else if (id == R.id.nav_add) {
            title = getString(R.string.add_title_section);
            fragment = addBathroomFragment;
            fragmentTitle = "addBathroom";
            mFab.show();
            bottomSheet.setVisibility(View.INVISIBLE);
        } else if (id == R.id.nav_feedback) {
            title = getString(R.string.feedback_title_section);
            fragment = new FeedbackFormFragment();
            fragmentTitle = "feedback";
            mFab.show();
            bottomSheet.setVisibility(View.INVISIBLE);
        }

        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, fragment)
                    .addToBackStack(fragmentTitle)
                    .commit();
            setToolbarTitle(title);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}