package org.refugerestrooms.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.ProgressBar;
import android.widget.SearchView;

import org.refugerestrooms.models.Bathroom;


/**
 * Created by Briar Rose Schreiber on 11/16/15.
 */
public class ArrayAdapterSearchView extends SearchView {

    private AutoCompleteTextView mSearchAutocomplete;
    private BathroomAutocompleteAdapter mBathroomAutocompleteAdapter;
    private ProgressBar mLoadingIndicator;

    private OnQueryTextListener mOnQueryTextListener = new OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mBathroomAutocompleteAdapter.notifyDataSetInvalidated();
            return false;
        }
    };

    public void setLoadingIndicator(ProgressBar view) {
        mLoadingIndicator = view;
    }

    public void showLoading() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        mLoadingIndicator.setVisibility(View.GONE);
    }

    public ArrayAdapterSearchView(Context context) {
        super(context);
        initialize();
    }

    public ArrayAdapterSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public void initialize() {
        int searchResourceId = getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        mSearchAutocomplete = (AutoCompleteTextView) findViewById(searchResourceId);

        mBathroomAutocompleteAdapter = new BathroomAutocompleteAdapter(
                getContext(),
                searchResourceId
        );

        setAutoCompleSuggestionsAdapter(mBathroomAutocompleteAdapter);
        setOnQueryTextListener(mOnQueryTextListener);
    }

    public Bathroom getItemFromAdapter(int position) {
        return mBathroomAutocompleteAdapter.getItem(position);
    }

    @Override
    public void setSuggestionsAdapter(CursorAdapter adapter) {
        throw new UnsupportedOperationException(
                "Please use setAutoCompleSuggestionsAdapter(ArrayAdapter<?> adapter) instead"
        );
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mSearchAutocomplete.setOnItemClickListener(listener);
    }

    public void setAutoCompleSuggestionsAdapter(ArrayAdapter<?> adapter) {
        mSearchAutocomplete.setAdapter(adapter);
    }

    public void setText(String text) {
        mSearchAutocomplete.setText(text);
    }


}
