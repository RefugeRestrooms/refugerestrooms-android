package org.refugerestrooms.android.view;

import org.refugerestrooms.android.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;
import android.text.util.Linkify;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	/**
	 * Launch the Add loo activity
	 * @param view
	 */
	public void onClickAdd(View view) {
		Intent intent = new Intent(this, AddActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Search by location
	 * @param view
	 */
	public void onClickLocation(View view) {
		Intent intent = new Intent(this, AddActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Launch the about activity
	 * @param view
	 */
	public void onClickAbout(View view) {
 		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	/**
	 * Launch the Search activity
	 * @param view
	 */
	public void onClickSearch(View view) {
		Intent intent = new Intent(this, ListSearchActivity.class);
		EditText searchField = (EditText) findViewById(R.id.searchText);
		String searchTerm = searchField.getText().toString();
		intent.putExtra(ListSearchActivity.INTENT_EXTRA_SEARCH_PARAMS, searchTerm);
		startActivity(intent);
	}
}
