package org.refugerestrooms.android.server;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.refugerestrooms.android.model.Bathroom;

import android.location.Address;


public class Server {

	private ServerListener mListener;
	
	public Server(ServerListener mListener) {
		super();
		this.mListener = mListener;
	}
	
	public void performSearch(String searchTerm) {
		// TODO Lark around on the internet
		
		List<Bathroom> results = new LinkedList<Bathroom>();
		results.add(new Bathroom("High St Public Bathroom", new Address(Locale.getDefault()), false, true, "Public toilet outside the library", "Bring your own T.P."));
		results.add(new Bathroom("Leisure Centre Bathroom", new Address(Locale.getDefault()), true, false, "Just off the lobby", "Swimwear optional"));
		if (mListener != null) {
			mListener.onSearchResults(results);
		}
	}

	public void submitNewEntry() {
		// TODO Lark around on the internet
		if (mListener != null) {
			mListener.onSubmission(true);
		}
	}
	
	public interface ServerListener {
		public void onSearchResults(List<Bathroom> results);
		public void onSubmission(boolean success);
	}
	
}
