package org.refugerestrooms.alt;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class ListSearchByLocationActivity extends ListSearchActivity implements LocationListener {

    protected LocationManager mLocationManager;
    protected Location mLastKnownLocation;

    @Override
    protected void doSearch(Bundle extras) {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        mLastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        String latLng = mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude();
        performSearch(latLng, true);

        Log.d("Captain's log", "latLng - " + latLng);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(mLastKnownLocation != location) {
            String latLng = location.getLatitude() + "," + location.getLongitude();
            performSearch(latLng, true);
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
}
