package org.refugerestrooms.android.view;

import java.util.List;

import org.refugerestrooms.android.model.Bathroom;
import org.refugerestrooms.android.server.Server;
import org.refugerestrooms.android.server.Server.ServerListener;

import com.jmpumphrey.refugerestrooms.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ListSearchActivity extends ActionBarActivity implements ServerListener {
	public static final String INTENT_EXTRA_SEARCH_PARAMS = "search"; //TODO one of these for each search param
	
	private Server mServer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_search);
		
		mServer = new Server(this);

	    ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	    Bundle extras = getIntent().getExtras();
	    if (extras != null) {
	    	String searchTerm = extras.getString(INTENT_EXTRA_SEARCH_PARAMS);
	    	mServer.performSearch(searchTerm);
	    }
	    //TODO save results
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
		ArrayAdapter<Bathroom> adapter = new BathroomListAdapter(getApplicationContext(), R.layout.list_entry, R.id.list_item_text, results);
		
		((ListView) findViewById(R.id.list_view)).setAdapter(adapter);
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
				Toast.makeText(ListSearchActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
			}
		});
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
