package org.refugerestrooms.application;

/**
 * Created by Ahmed Fahmy on 3/1/2016.
 */

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.refugerestrooms.database.model.DaoSession;
import org.refugerestrooms.database.model.DatabaseInitHandler;

public class RefugeRestroomApplication extends Application {
    private static RefugeRestroomApplication instance;
    /**
     * To be used as a static request queue across the application, this improves
     * the application efficiency.
     */
    private static RequestQueue mRequestQueue;

    private static Context context;

    private DatabaseInitHandler databaseInitHandler;
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();


        init(getApplicationContext());

    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public void init(Context context) {
        instance = this;
        this.context = context;
        mRequestQueue = Volley.newRequestQueue(context);
        databaseInitHandler = new DatabaseInitHandler();
        databaseInitHandler.initDataBase(context);
        daoSession = databaseInitHandler.getDaoSession();
    }

    public static RefugeRestroomApplication getInstance() {
        if (instance == null) {
            instance = new RefugeRestroomApplication();
        }
        return instance;
    }

    public static RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        } else if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
        return mRequestQueue;
    }



}
