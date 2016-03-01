package org.refugerestrooms.application;

/**
 * Created by Ahmed Fahmy on 3/1/2016.
 */

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.Tracker;

public class RefugeRestroomApplication extends Application {
    private static RefugeRestroomApplication instance;
    private static RequestQueue mRequestQueue;

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();


        init(getApplicationContext());
    }

    public void init(Context context) {
        this.context = context;
        mRequestQueue = Volley.newRequestQueue(context);

    }

    public static RefugeRestroomApplication getInstance() {
        if (instance == null) {
            instance = new RefugeRestroomApplication();
        }
        return instance;
    }

    public static RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(context);
        } else if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
        return mRequestQueue;
    }



}
