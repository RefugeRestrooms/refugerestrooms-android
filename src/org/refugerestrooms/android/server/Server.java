package org.refugerestrooms.android.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.refugerestrooms.android.model.Bathroom;

import android.location.Address;
import android.os.AsyncTask;
import android.util.Log;


public class Server {

	protected static final String TAG = null;
	private ServerListener mListener;
	
	public Server(ServerListener mListener) {
		super();
		this.mListener = mListener;
	}
	

	@SuppressWarnings("unused")
	public void performSearch(final String searchTerm) {
		// TODO Lark around on the internet
		
		new RemoteCallTask() {
			private String searchTerm;

			private RemoteCallTask setSearchTerm(String searchTerm) {
				this.searchTerm = searchTerm;
				return this;
			}
			
			@Override
			public URI buildUrl() throws URISyntaxException {
				return new URI("http://www.refugerestrooms.org/api/v1/bathrooms");
			}
		}.setSearchTerm(searchTerm).execute();
		
//		List<Bathroom> results = new LinkedList<Bathroom>();
//		results.add(new Bathroom("High St Public Bathroom", new Address(Locale.getDefault()), false, true, "Public toilet outside the library", "Bring your own T.P.", 100));
//		results.add(new Bathroom("Leisure Centre Bathroom", new Address(Locale.getDefault()), true, false, "Just off the lobby", "Swimwear optional", 0));
//		results.add(new Bathroom("Bathroom in the Duke's Head", new Address(Locale.getDefault()), true, true, "To the right of the bar", "You should probably buy a drink", 68));
//		if (mListener != null) {
//			mListener.onSearchResults(results);
//		}
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

		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "Result: " + result);
			if (mListener != null) {
				mListener.onSearchResults(new LinkedList<Bathroom>());
			}
		}
	}

	public interface ServerListener {
		public void onSearchResults(List<Bathroom> results);
		public void onSubmission(boolean success);
		public void onError(String errorMessage);
	}
	
}
