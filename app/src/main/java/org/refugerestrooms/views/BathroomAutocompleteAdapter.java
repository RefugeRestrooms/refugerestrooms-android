package org.refugerestrooms.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.refugerestrooms.R;
import org.refugerestrooms.models.Bathroom;
import org.refugerestrooms.servers.Server;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Briar Rose Schreiber on 11/16/15.
 */
public class BathroomAutocompleteAdapter extends ArrayAdapter
        implements Filterable, Server.ServerListener {

    private ArrayList<Bathroom> mResultList;
    private final ArrayList<Bathroom> mEmptyList = new ArrayList<>();
    private Server mServer;
    private LayoutInflater mInflator;

    public BathroomAutocompleteAdapter(Context context, int view) {
        super(context, view);
        mServer = new Server(this);
        mResultList = new ArrayList<>();
        mInflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Bathroom bathroom = getItem(position);

        if (convertView == null) {
            convertView = mInflator.inflate(R.layout.bathroom_result, null);

            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.bathroom_name);
            holder.local = (TextView) convertView.findViewById(R.id.bathroom_local);
            holder.isAccessable = (TextView) convertView.findViewById(R.id.is_accessible);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (bathroom != null) {
            holder.name.setText(bathroom.getName());
            holder.local.setText(bathroom.getAddress());
            if (bathroom.isAccessible()) {
                holder.isAccessable.setVisibility(View.VISIBLE);
            } else {
                holder.isAccessable.setVisibility(View.GONE);
            }
        } else {
            holder.name.setText("Bathroom");
            holder.local.setText("");
            holder.isAccessable.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mResultList.size();
    }

    @Override
    public Bathroom getItem(int position) {
        return mResultList.get(position);
    }

    @Override
    public Filter getFilter() {
        final Filter filter = new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();

                if (constraint != null && constraint.length() > 2) {
                    autocomplete(constraint.toString());
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                // this is handled in a callback specified in the call to autocomplete
            }
        };

        return filter;
    }

    private void autocomplete(String constraint) {
        mServer.performSearch(constraint, false);
    }

    @Override
    public void onSearchResults(List<Bathroom> results) {
        notifyDataSetInvalidated();
        mResultList.clear();
        mResultList.addAll(results);
        notifyDataSetChanged();
    }

    public void onSubmission(boolean success) {
        // nothing
    }

    public void onError(String errorMessage) {
        Log.d(getClass().getName(), errorMessage);
    }

    private static class ViewHolder {
        public TextView name;
        public TextView local;
        public TextView isAccessable;
    }
}
