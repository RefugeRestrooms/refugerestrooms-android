package org.refugerestrooms.database.model;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.refugerestrooms.database.model.DaoMaster.DevOpenHelper;
/**
 * Created by Ahmed Fahmy on 3/2/16.
 */
public class DatabaseInitHandler {

    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private BathroomEntityDao bathroomEntityDao;
    private static final String BATHROOMS_DATABASE_NAME = "restrooms-db";
    private Cursor cursor;
    private SQLiteDatabase db;


    public DaoMaster getDaoMaster() {
        return daoMaster;
    }

    public void setDaoMaster(DaoMaster daoMaster) {
        this.daoMaster = daoMaster;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public void setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
    }

    public BathroomEntityDao getBathroomEntityDao() {
        return bathroomEntityDao;
    }

    public void setBathroomEntityDao(BathroomEntityDao bathroomEntityDao) {
        this.bathroomEntityDao = bathroomEntityDao;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        this.cursor = cursor;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public void setDb(SQLiteDatabase db) {
        this.db = db;
    }


    public void initDataBase(Context context){
        DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, BATHROOMS_DATABASE_NAME, null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
        bathroomEntityDao = daoSession.getBathroomEntityDao();
    }
}
