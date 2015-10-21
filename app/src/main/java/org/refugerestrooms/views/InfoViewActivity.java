package org.refugerestrooms.views;

/**
 * Created by Refuge Restrooms on 9/26/15.
 */
        import org.refugerestrooms.models.Bathroom;

        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.MapFragment;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.maps.model.MarkerOptions;
        import org.refugerestrooms.R;

        import android.graphics.Typeface;
        import android.os.Bundle;
        import android.support.v7.app.ActionBar;
        import android.support.v7.app.ActionBarActivity;
        import android.text.Html;
        import android.text.TextUtils;
        import android.util.Log;
        import android.view.Gravity;
        import android.view.View;
        import android.widget.TextView;

public class InfoViewActivity extends ActionBarActivity {

    public static final String EXTRA_BATHROOM = "bathroom";
    protected static final String TAG =  InfoViewActivity.class.getSimpleName();
    private Bathroom mBathroom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (getIntent().getExtras() != null) {
            mBathroom = Bathroom.fromJson(getIntent().getExtras().getString(EXTRA_BATHROOM));
        }

        String name = mBathroom.getName();
        if (name != null) {
            setTitle(name);
        }

        updateView();
    }

    private void updateView() {
        if (mBathroom != null) {
            TextView tv = (TextView) findViewById(R.id.text_title);
            tv.setText(getBathroomTitle());
            TextView tv2 = (TextView) findViewById(R.id.text_address);
            tv2.setText(getBathroomAddress());
            TextView tv3 = (TextView) findViewById(R.id.text_comments);
            tv3.setText(Html.fromHtml((String) getBathroomComments()));
            tv.setGravity(Gravity.CENTER);
            tv2.setGravity(Gravity.CENTER);
            tv3.setGravity(Gravity.CENTER);
            View specsView = findViewById(R.id.specs);
            BathroomSpecsViewUpdater.update(specsView, mBathroom, this);
        }
    }

    private CharSequence getBathroomTitle() {
        String text = "";
        String name = mBathroom.getName();
        if (!TextUtils.isEmpty(name)) {
            text += name;
        }
        return  text;
    }
    private CharSequence getBathroomAddress() {
        String text = "";
        String address = mBathroom.getAddress();
        if (!TextUtils.isEmpty(address)) {
            text += address;
        }
        return  text;
    }
    private CharSequence getBathroomComments() {
        String text = "";
        String directions = mBathroom.getDirections();
        String comments = mBathroom.getComments();

        if (!TextUtils.isEmpty(directions)) {
            text += "<br><b>Directions</b><br><br>" + directions + "<br><br>";
        }
        if (!TextUtils.isEmpty(comments)) {
            text += "<br><b>Comments</b><br><br>" + comments;
        }

        return  text;
    }


}
