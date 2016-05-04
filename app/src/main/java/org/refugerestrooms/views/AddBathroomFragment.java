package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 7/14/2015.
 */
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import org.refugerestrooms.R;

public class AddBathroomFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.add_bathroom, container, false);
        WebView mWebView = (WebView) rootView.findViewById(R.id.addBathroom);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new AddBathroomClient("http://www.refugerestrooms.org/restrooms/new?"));
        mWebView.loadUrl("http://www.refugerestrooms.org/restrooms/new?");
        // Inflate the layout for this fragment
        return rootView;

        /*View view = inflater.inflate(R.layout.fragment_add_bathroom, container, false);

        Spinner countries = (Spinner) view.findViewById(R.id.add_bathroom_country);
        ArrayAdapter<CharSequence> countryAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.countries, android.R.layout.simple_spinner_dropdown_item);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countries.setAdapter(countryAdapter);

        Spinner accessible = (Spinner) view.findViewById(R.id.add_bathroom_accessible);
        ArrayAdapter<CharSequence> accessibleAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.accessibility, android.R.layout.simple_spinner_dropdown_item);
        accessibleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accessible.setAdapter(accessibleAdapter);

        Spinner unisex = (Spinner) view.findViewById(R.id.add_bathroom_unisex);
        ArrayAdapter<CharSequence> unisexAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.unisex, android.R.layout.simple_spinner_dropdown_item);
        unisexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unisex.setAdapter(unisexAdapter);

        ((Button) view.findViewById(R.id.add_bathroom_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO submit a bathroom

            }
        });

        return view;*/
    }
}