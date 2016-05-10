package org.refugerestrooms.servers;

import android.net.Uri;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.refugerestrooms.application.RefugeRestroomApplication;
import org.refugerestrooms.models.Bathroom;
import org.refugerestrooms.models.ListOfBathrooms;

import java.net.URISyntaxException;
import java.util.List;

/**
 * Placeholder
 *
 * @author Refuge Restrooms
 *
 * @edited @Sifrazooy
 *   Old HTTP requests is now removed and volley is used instead but the same listener is still
 *   used.
 */

public class Server {

    protected static final String TAG = null;
    private static final String SERVER_URL = "http://www.refugerestrooms.org:80/api/v1/";

    private final ServerListener mListener;

    private final Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            mListener.onError(error.getMessage());
        }
    };

    private final Response.Listener<JSONArray> onSuccessResponseListener = new Response.Listener<JSONArray>() {
        @Override
        public void onResponse(JSONArray response) {
            try {
                String responseStr = response.toString();
                Gson gson = new Gson();
                ListOfBathrooms list = gson.fromJson(responseStr, ListOfBathrooms.class);

                if (mListener != null) {
                    mListener.onSearchResults(list);
                }
            } catch (JsonSyntaxException jse) {
                String msg = "JSON Error: " + jse.getMessage();
                //Log.e(TAG, msg);
                reportError(msg);
            }
        }
    };

    public Server(ServerListener mListener) {
        super();
        this.mListener = mListener;
    }

    private String buildUrl(String searchTerm, boolean isLocation) throws URISyntaxException {
        if (isLocation) {
            // Refuge Restrooms API bathrooms queried
            // http://www.refugerestrooms.org/api/docs/#!/restrooms/GET_version_restrooms_search_format
            // limit per_page=20 so only the 20 nearest relevant results display for search

            return SERVER_URL + "restrooms/by_location.json?per_page=20&" + searchTerm;
        } else {
            // limit per_page=75 so only the 75 most relevant results display for search
            return SERVER_URL + "restrooms/search.json?per_page=75&query=" + Uri.encode(searchTerm, "UTF-8");
        }
    }


    public void performSearch( String searchTerm,  boolean location) {


        try {
            String url = buildUrl(searchTerm, location);
            RequestHandler.requestJsonArray(onSuccessResponseListener, errorListener, RefugeRestroomApplication.getRequestQueue(), url);


        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


    }

    protected void reportError(String errorMessage) {
        if (mListener != null) {
            mListener.onError(errorMessage);
        }
    }



    public interface ServerListener {
        public void onSearchResults(List<Bathroom> results);

        public void onSubmission(boolean success);

        public void onError(String errorMessage);
    }

}
