package org.refugerestrooms.servers;

/**
 * Created by Refuge Restrooms on 7/13/2015.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.refugerestrooms.models.Bathroom;
import org.refugerestrooms.models.ListOfBathrooms;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


public class JsonRequest {

    protected static final String TAG = null;
    private ServerListener mListener;

    public JsonRequest(ServerListener mListener) {
        super();
        this.mListener = mListener;
    }


    @SuppressWarnings("unused")
    public void performSearch() {
        // TODO Lark around on the internet

        new RemoteCallTask() {


            @Override
            public URI buildUrl() throws URISyntaxException {
                return new URI("http://www.refugerestrooms.org:80/api/v1/restrooms.json?unisex=1");
            }

            @Override
            protected void onPostExecute(String result) {
                Log.d(TAG, "Result: " + result);
                if (result != null) {
                    try {
                        Gson gson = new Gson();
                        ListOfBathrooms list = gson.fromJson(result, ListOfBathrooms.class);

                        if (mListener != null) {
                            mListener.onSearchResults(list);
                        }
                    } catch (JsonSyntaxException jse) {
                        String msg = "JSON Error: " + jse.getMessage();
                        Log.e(TAG, msg);
                        reportError(msg);
                    }
                } else {
//					List<Bathroom> results = new LinkedList<Bathroom>();
//					results.add(new Bathroom("High St Public Bathroom", new Address(Locale.getDefault()), false, true, "Public toilet outside the library", "Bring your own T.P.", 100));
//					results.add(new Bathroom("Leisure Centre Bathroom", new Address(Locale.getDefault()), true, false, "Just off the lobby", "Swimwear optional", 0));
//					results.add(new Bathroom("Bathroom in the Duke's Head", new Address(Locale.getDefault()), true, true, "To the right of the bar", "You should probably buy a drink", 68));
//					results.add(new Bathroom("Bathroom in the Horse and Jockey", new Address(Locale.getDefault()), true, true, "Upstairs", "They have some nice craft beers", 25));
//					results.add(new Bathroom("Bathroom in the Wheatsheaf", new Address(Locale.getDefault()), true, true, "In the basement", "A bit dark", 50));
//
//					if (mListener != null) {
//						mListener.onSearchResults(results);
//					}
                }
            }

        };
    }

    protected void reportError(String errorMessage) {
        if (mListener != null) {
            mListener.onError(errorMessage);
        }
    }

    public void submitNewEntry() {
        // TODO Lark around on the internet
        if (mListener != null) {
            mListener.onSubmission(true);
        }
    }

    private abstract class RemoteCallTask extends
            AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... arg0) {
            HttpGet request;
            try {
                request = new HttpGet(buildUrl());
                HttpClient client = new DefaultHttpClient();
                HttpResponse response = client.execute(request);
                int code = response.getStatusLine().getStatusCode();

                if (code == HttpStatus.SC_OK) {
                    return EntityUtils.toString(response.getEntity());
                } else {
                    reportError("Failed with HTTP code " + code);
                }
            } catch (ClientProtocolException e) {
                Log.e(TAG, e.getMessage());
                reportError("ClientProtocolException");
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                reportError("IOException");
            } catch (URISyntaxException e1) {
                Log.e(TAG, "Failed to build URL " + e1.getMessage());
            }
            return null;
        }

        public abstract URI buildUrl() throws URISyntaxException;
    }

    public interface ServerListener {
        public void onSearchResults(List<Bathroom> results);
        public void onSubmission(boolean success);
        public void onError(String errorMessage);
    }

}

