package org.refugerestrooms.views;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.refugerestrooms.R;
import org.refugerestrooms.models.Bathroom;
import org.refugerestrooms.models.Bathroom;
import org.refugerestrooms.models.ListOfBathrooms;
import org.refugerestrooms.servers.Server;

import java.util.List;

/**
 * Created by Refuge Restrooms on 7/26/15.
 */
public class SearchResultsActivity extends Activity
        implements Server.ServerListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search
            Server mServer = new Server(this);
            mServer.performSearch(query, false);
        }
    }
    // Adds bathrooms from json query
    LatLng[] locations;
    String[] names;
    int numLocations;


    //Listener for the server
    public void onSearchResults(List<Bathroom> results) {
        locations = new LatLng[results.size()];
        names = new String[results.size()];
        numLocations = results.size();

        for (int i = 0; i < numLocations; i++)
        {
            Bathroom bathroom = results.get(i);
            LatLng temp = bathroom.getLocation();
            String name = bathroom.getName();
            //String comment = bathroom.getComments();
            // Adds bathroom markers, blue for accessible, red for not
/*
            if (bathroom.isAccessible() == true)
            {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(temp.latitude, temp.longitude))
                        .title(bathroom.getName())
                        .snippet(bathroom.getDirections())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
            else
            {
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(temp.latitude,temp.longitude))
                        .title(bathroom.getName())
                        .snippet(bathroom.getDirections())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
*/

            locations[i] = temp;
            names[i] = name;
        }
        Toast.makeText(this, names[0],
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSubmission(boolean success) {

    }

    @Override
    public void onError(String errorMessage) {

    }


}