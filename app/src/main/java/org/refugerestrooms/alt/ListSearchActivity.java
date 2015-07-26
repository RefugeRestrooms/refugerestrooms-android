package org.refugerestrooms.alt;

import java.util.List;

import org.refugerestrooms.models.Bathroom;
import org.refugerestrooms.alt.ServerAlt;
import org.refugerestrooms.alt.ServerAlt.ServerListener;

import org.refugerestrooms.R;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ProgressBar;

public class ListSearchActivity extends ActionBarActivity implements ServerListener {
	public static final String INTENT_EXTRA_SEARCH_PARAMS = "search"; //TODO one of these for each search param
	
	private ServerAlt mServerAlt;
	private String mSearchTerm;
    private static final String KEY_QUERY = "query";
    protected static final String TAG =  ListSearchActivity.class.getSimpleName();

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Log.d(TAG, "Creating List");
		setContentView(R.layout.activity_list_search);
        if (savedInstanceState != null) {
            Log.d(TAG,"Using Saved instance state");
            mSearchTerm = savedInstanceState.getString(KEY_QUERY);
        }

		mServerAlt = new ServerAlt(this);
	    ActionBar actionBar = getSupportActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);

        // Create a progress bar to display while the list loads
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
		progressBar.setVisibility(View.VISIBLE);

	    Bundle extras = getIntent().getExtras();
		doSearch(extras);
    }

	protected void doSearch(Bundle extras) {
		if (extras != null) {
			String searchTerm = (!extras.containsKey("query")) ?
					extras.getString(INTENT_EXTRA_SEARCH_PARAMS)
					: extras.getString("query");
			mSearchTerm = searchTerm; //save query so we can return to activity later
			mServerAlt.performSearch(searchTerm, false);
		}
	}

	protected void performSearch(String searchTerm, boolean location) {
		mServerAlt.performSearch(searchTerm, location);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG,"Create Options Menu");
        return super.onCreateOptionsMenu(menu);

	}
	
	private void launchDetails(Bathroom bathroom) {
		//TODO add bathroom details
        Log.d(TAG,"Launch Details");
		Intent intent = new Intent(this, DetailViewActivity.class);
		intent.putExtra(DetailViewActivity.EXTRA_BATHROOM, bathroom.toJson());
		startActivity(intent);
	}

    //Listener for the server
    @Override
    public void onSearchResults(List<Bathroom> results) {
        Log.d(TAG, "Search Results");
        ArrayAdapter<Bathroom> adapter = new BathroomListAdapter(getApplicationContext(), R.layout.list_entry, R.id.list_item_text, results);

        ListView list = (ListView) findViewById(R.id.list_view);
        list.setEmptyView(findViewById(R.id.no_results));
        list.setAdapter(adapter);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
		progressBar.setVisibility(ProgressBar.GONE);
    }


	@Override
	public void onSubmission(boolean success) {
        Log.d(TAG, "Submission");
		//nothing
	}
	
	@Override
	public void onError(final String errorMessage) {
        Log.d(TAG, "on Error");
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
			BathroomSpecsViewUpdater.update(view, bathroom, getContext());
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's search term
        savedInstanceState.putString(KEY_QUERY, mSearchTerm);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

        @Override
        public void onResume(){
            //Your code here
            Log.d(TAG,"On Resume");
            super.onResume();
        }
}
